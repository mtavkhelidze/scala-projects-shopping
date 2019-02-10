import io.circe.generic.auto._
import io.circe.parser.decode
import models.Cart
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.libs.ws.{ DefaultWSCookie, WSClient }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }
import scala.language.postfixOps

class APIControllerSpec extends PlaySpec with ScalaFutures with GuiceOneServerPerSuite {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  // port is implicit in GuiceOneServerPerSuite base class
  val base = s"http://localhost:$port/api"
  val products = s"$base/products"
  val cart = s"$base/cart"

  val allProductInCart = s"$cart/products"

  def productInCart(id: String) = s"$cart/products/$id"

  def productQtyInCart(id: String, quantity: Int) =
    s"$cart/products/$id/quantity/$quantity"

  val login = s"$base/login"

  "The API" should {
    val client = app.injector.instanceOf[WSClient]
    lazy val cookie: DefaultWSCookie = {
      val cookie = client.url(login).post("me").map { res =>
        res.headers.get("Set-Cookie").map(_.head.split(";").head)
      }.futureValue
      val sid = cookie.get.split("=").tail.mkString
      DefaultWSCookie("PLAY_SESSION", sid)
    }

    "list all products" in {
      val res = client.url(products).get().futureValue
      res.status mustBe OK

      res.body must include("pepper")
      res.body must include("nao")
      res.body must include("beobot")
    }

    "add a product" in {
      val newProduct =
        """
          |{  "name":"NewOne",
          |   "code":"New",
          |   "description":"The brand new product",
          |   "price":100.0
          |}
          |""".stripMargin
      val posted = client.url(products).post(newProduct).futureValue
      posted.status mustBe OK

      val res = client.url(products).get().futureValue
      res.body must include("NewOne")
    }

    "add a product in the cart" in {
      val id = "ald1"
      val qty = 1
      val posted = client.url(productQtyInCart(id, qty)).post("").futureValue
      posted.status mustBe OK
    }

    "delete a product from the cart" in {
      val id = "ald1"
      val posted = client.url(productInCart(id)).delete.futureValue
      posted.status mustBe OK
    }

    "update a product in the cart" in {
      val id = "ald1"
      var qty = 1
      var posted = client.url(productQtyInCart(id, qty)).put("").futureValue
      posted.status mustBe OK

      qty = 99
      posted = client.url(productQtyInCart(id, qty)).put("").futureValue
      posted.status mustBe OK
    }

    "return a cookie, when user logs in" in {
      val cookie = client.url(login).post("myId").map { res =>
        res.headers.get("Set-Cookie").map(
          h => h.head.split(";").filter(_.startsWith("PLAY_SESSION")).head
        )
      }.futureValue
      val key = cookie.get.split("=").head
      key mustEqual "PLAY_SESSION"
    }

    "list all products in the cart" in {
      val resp = client.url(allProductInCart).addCookies(cookie).get.futureValue

      resp.status mustBe OK

      val prods = decode[Seq[Cart]](resp.body)

      prods.right.get mustBe empty
    }

    "add product to the cart" in {
      val id = "ald1"
      val qty = 1
      val posted = client
          .url(productQtyInCart(id, qty))
          .addCookies(cookie)
          .post("")
          .futureValue
      posted.status mustBe OK

      val resp = client.url(allProductInCart).addCookies(cookie).get.futureValue
      resp.status mustBe OK
      resp.body must include(id)
    }

    "delete product from the cart" in {
      val id = "ald1"
      val posted = client
          .url(productInCart(id))
          .addCookies(cookie)
          .delete
          .futureValue
      posted.status mustBe OK

      val resp = client.url(allProductInCart).addCookies(cookie).get.futureValue
      resp.status mustBe OK
      resp.body mustNot include(id)
    }

    "update product quantity in the cart" in {
      val id = "ald1"
      val qty = 1
      val posted = client
          .url(productQtyInCart(id, qty))
          .addCookies(cookie)
          .post("")
          .futureValue
      posted.status mustBe OK

      val newQty = 99
      val update = client
          .url(productQtyInCart(id, newQty))
          .addCookies(cookie)
          .put("")
          .futureValue
      update.status mustBe OK

      val res = client
          .url(allProductInCart)
          .addCookies(cookie)
          .get
          .futureValue
      res.status mustBe OK
      res.body must include(id)
      res.body must include(newQty.toString)
    }
  }
}
