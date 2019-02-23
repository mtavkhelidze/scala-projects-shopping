package ge.zgharbi.shopping.client

import ge.zgharbi.shopping.shared.Product
import org.scalajs.dom.html.Div
import scalatags.JsDom.all._

case class ProductDiv(product: Product) {

  private def addToCart = () => UIManager.addOneProduct(product)

  private def getButton =
    button(`type` := "button", onclick := addToCart)("Add to Cart")

  private def getProductDescription = div(
    p(product.name),
    p(product.description),
    p(product.price)
  )

  def content: Div = div(`class` := "col")(getProductDescription, getButton)
      .render
}
