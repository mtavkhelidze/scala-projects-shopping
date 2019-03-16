package ge.zgharbi.shopping.shared

sealed trait CartAction

case object Add extends CartAction

case object Remove extends CartAction

sealed trait WSMessage

case class CartEvent(
    user: String, product: Product, action: CartAction
) extends WSMessage

case class Alarm(message: String, action: CartAction) extends WSMessage
