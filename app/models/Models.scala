package models

case class Product(code: String, name: String, description: String, price: Double)

case class Cart(user: String, productCode: String, quantity: Int)
