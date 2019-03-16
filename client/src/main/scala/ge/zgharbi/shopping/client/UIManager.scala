package ge.zgharbi.shopping.client

import ge.zgharbi.shopping.shared._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.{ Event, MessageEvent, WebSocket }
import org.scalajs.dom.html.Document

import scala.scalajs.js.UndefOr
import scala.util.{ Random, Try }

object UIManager {

  val ws: WebSocket = getWebSocket

  private def notifyUser(alarm: Alarm) = {
    val notifyClass = if (alarm.action == Add) "info" else "warn"
    NotifyJS.notify(
      alarm.message, new Options {
        className = notifyClass
        globalPosition = "right bottom"
      }
    )
  }

  private def getWebSocket: WebSocket = {
    val ws = new WebSocket(getWebSocketURI(dom.document, "api/cart/events"))

    ws.onopen = { event: Event =>
      println(s"Opened `${event.`type`}`")
      event.preventDefault;
    }

    ws.onerror = { event: Event =>
      System.err.println(s"Error opening WebSocket: `${event.getClass}`")
    }

    ws.onmessage = { event: MessageEvent =>
      println(s"WebSocket message: ${event.data.toString}")
      val msg = decode[Alarm](event.data.toString)
      msg match {
        case Right(alarm) =>
          notifyUser(alarm)
        case Left(_) =>
          println(s"Unknown message: ${event.data.toString}")
      }
    }

    ws.onclose = { event: Event =>
      println(s"Close WebSocket: ${event.`type`}")
    }

    ws
  }

  private def getWebSocketURI(document: Document, path: String): String = {
    val proto =
      if (dom.document.location.protocol == "https")
        "wss"
      else
        "ws"
    s"$proto://${document.location.host}/$path"
  }


  private def putInCart(code: String, qty: Int): JQueryDeferred = {
    val url = s"${UIManager.origin}/api/cart/products/$code/quantity/$qty"
    println(url)
    $.ajax(JQueryAjaxSettings.url(url).method("PUT")._result).done()
  }

  private def quantity(code: String): Int =
    Try {
      val inp = $(s"#cart-$code-qty")
      if (inp.length != 0) {
        val p = Integer.parseInt(inp.`val`().asInstanceOf[String])
        println(p)
        p
      } else
        10
    }.getOrElse(13)

  def updateProduct(product: Product): JQueryDeferred =
    putInCart(product.code, quantity(product.code))

  def deleteFromCart(product: Product): JQueryDeferred = {
    def onDone = () => {
      val cart = $(s"#cart-${product.code}-row")
      cart.remove()
      println(s"Product $product removed from the cart")
      ws.send(CartEvent(user, product, Remove).asJson.noSpaces)
    }

    deleteProductFromCart(product.code, onDone)
  }

  def addOneProduct(product: Product): JQueryDeferred = {
    val qty = 1

    def onDone(): Unit = {
      val cartContent = cart.addProduct(CartLine(qty, product)).content
      $("#cartPanel").append(cartContent)
      ws.send(CartEvent(user, product, Add).asJson.noSpaces)

    }

    postInCart(product.code, qty, onDone)
  }

  private def deleteProductFromCart(code: String, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/api/cart/products/$code"
    $.ajax(JQueryAjaxSettings.url(url).method("DELETE")._result)
        .done(onDone)
        .fail(() => println("Cannot remove product"))
  }

  private def postInCart(code: String, qty: Int, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/api/cart/products/$code/quantity/$qty"
    $.post(JQueryAjaxSettings.url(url)._result)
        .done(onDone)
        .fail(() => println("cannot add product"))
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

  private def initUI(origin: UndefOr[String]) =
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

  private def initCartUI(origin: UndefOr[String], products: Seq[Product]) =
    $.get(url = s"$origin/api/cart/products", dataType = "text")
        .done(
          (answers: String) => {
            val carts = decode[Seq[Cart]](answers)
            carts.right.map {
              cartLines =>
                cartLines.foreach {
                  cartDao =>
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
