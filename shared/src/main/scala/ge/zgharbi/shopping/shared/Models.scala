package ge.zgharbi.shopping.shared

case class Product(code: String, name: String, description: String, price: Double)

case class Cart(user: String, productCode: String, quantity: Int)
