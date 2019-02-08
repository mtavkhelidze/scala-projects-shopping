import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.libs.ws.WSClient

import scala.concurrent.duration.DurationInt
import scala.concurrent.Await
import scala.language.postfixOps

class APIControllerSpec extends PlaySpec with ScalaFutures with GuiceOneServerPerSuite {
  // port is implicit in GuiceOneServerPerSuite base class
  val base = s"http://localhost:$port/api"
  val products = s"$base/products"
  val cart = s"$base/cart"

  val addProduct = s"$products"
  val allProductInCart = s"$cart/products"

  def deleteProductsInCart(id: String) = s"$cart/products/$id"

  def productInCart(id: String, quantity: Int) =
    s"$cart/products/$id/quantity/$quantity"


  "The API" should {
    val client = app.injector.instanceOf[WSClient]

    "list all products" in {
      val res = Await.result(client.url(products).get(), 1 seconds)
      res.status mustBe OK

      res.body must include("pepper")
      res.body must include("nao")
      res.body must include("beobot")
    }

    "add a product" in {
      val newProduct =
        """
          |{       "name": "NewOne",
          |        "code": "New",
          |        "description": "The brand new product",
          |        "price" : 100.0
          |}
          |""".stripMargin
      val posted = client.url(addProduct).post(newProduct).futureValue
      posted.status mustBe OK

      val res = client.url(products).get().futureValue
      res.body must include("NewOne")
    }

    "add a product in the cart" in {
      val id = "ald1"
      val qty = 1
      val posted = client.url(productInCart(id, qty)).post("").futureValue
      posted.status mustBe OK
    }

    "delete a product from the cart" in {
      val id = "ald1"
      val posted = client.url(deleteProductsInCart(id)).post("").futureValue
      posted.status mustBe OK
    }

    "update a product in the cart" in {
      val id = "ald1"
      var qty = 1
      var posted = client.url(productInCart(id, qty)).put("").futureValue
      posted.status mustBe OK

      qty = 99
      posted = client.url(productInCart(id, qty)).put("").futureValue
      posted.status mustBe OK
    }
  }
}
