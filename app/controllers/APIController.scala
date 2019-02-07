package controllers

import dao.ProductDao
import javax.inject.{ Inject, Singleton }
import play.api.mvc._
import play.mvc
import play.mvc.Results
import play.mvc.Result

@Singleton
class APIController @Inject()(
    cc: ControllerComponents, products: ProductDao
) extends AbstractController(cc) {
  def listProducts(): mvc.Result = Results.TODO

  def listCartProducts(): Result = Results.TODO

  def deleteCartProduct(id: String): Result = Results.TODO

  def addCartProduct(id: String, qty: String): Result = Results.TODO

  def updateCartProduct(id: String, qty: String): Result = Results.TODO

  def addProduct(): Result = Results.TODO
}
