package ge.zgharbi.shopping.client

import ge.zgharbi.shopping.shared.{ Cart, Product }
import io.circe.generic.auto._
import io.circe.parser._
import org.querki.jquery._
import org.scalajs.dom

import scala.scalajs.js.UndefOr
import scala.util.Random


object UIManager {

  private def postInCart(code: String, qty: Int, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/api/cart/products/$code/quantity/$qty"
    $.post(JQueryAjaxSettings.url(url)._result)
        .done(onDone)
        .fail(() => println("cannot add product"))
  }

  def addOneProduct(product: Product): JQueryDeferred = {
    val qty = 1

    def onDone(): Unit = {
      val cartContent = cart.addProduct(CartLine(qty, product)).content
      $("#cartPanel").append(cartContent)
    }
    postInCart(product.code, qty, onDone)
  }

  val origin: UndefOr[String] = dom.document.location.origin
  val cart: CartDiv = CartDiv(Set.empty[CartLine])
  val user = s"user-${Random.nextInt(1000)}"

  def main(args: Array[String]): Unit = {
    val settings = JQueryAjaxSettings
        .url(s"$origin/api/login")
        .data(user)
        .contentType("text/plain")
    $.post(settings._result).done((_: String) => initUI(origin))
  }

  private def initUI(origin: UndefOr[String]) = {

    $.get(url = s"$origin/api/products", dataType = "text")
        .done(
          (answers: String) => {
            val products = decode[Seq[Product]](answers)
            products.right.map { seq =>
              seq.foreach(
                p => {
                  $("#products").append(ProductDiv(p).content)
                }
              )
              initCartUI(origin, seq)
            }
          }
        )
        .fail(
          (xhr: JQueryXHR, textStatus: String, textError: String) =>
            println(
              s"call failed: $textStatus with status code: ${xhr.status} " +
                  s"$textError"
            )
        )
  }

  private def initCartUI(origin: UndefOr[String], products: Seq[Product]) = {
    $.get(url = s"$origin/api/cart/products", dataType = "text")
        .done(
          (answers: String) => {
            val carts = decode[Seq[Cart]](answers)
            carts.right.map { cartLines =>
              cartLines.foreach { cartDao =>
                val product = products.find(_.code == cartDao.productCode)
                product match {
                  case Some(p) =>
                    val cartLine = CartLine(cartDao.quantity, p)
                    val cartContent = UIManager.cart.addProduct(cartLine)
                        .content
                    $("#cartPanel").append(cartContent)
                  case None =>
                    println(
                      s"product code ${
                        cartDao
                            .productCode
                      } doesn't exists in the catalog"
                    )
                }
              }
            }
          }
        )
        .fail(
          (xhr: JQueryXHR, textStatus: String, textError: String) =>
            println(
              s"call failed: $textStatus with status code: ${xhr.status} " +
                  s"$textError"
            )
        )
  }
}
