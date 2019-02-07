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
  val productsInCart = s"$cart/products"

  def deleteProductsInCart(id: String) = s"$cart/products/$id"

  def updateProductsInCart(id: String, quantity: Int) =
    s"$cart/products/$id/quantity/$quantity"


  "The API" should {
    val client = app.injector.instanceOf[WSClient]

    "list all products" in {
      val resp = Await.result(client.url(products).get(), 1 seconds)
      resp.status mustBe OK
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
    }
  }
}
