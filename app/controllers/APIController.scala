package controllers

import dao.ProductDao
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.libs.circe.Circe
import play.api.mvc._
import play.mvc.{ Result, Results }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

@Singleton
class APIController @Inject()(
    cc: ControllerComponents, products: ProductDao
) extends AbstractController(cc) with Circe {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  def listProducts(): Action[AnyContent] = Action.async { _ =>
    for {
      ps <- products.all()
    } yield Ok(ps.asJson)
  }

  def listCartProducts(): Result = Results.TODO

  def deleteCartProduct(id: String): Result = Results.TODO

  def addCartProduct(id: String, qty: String): Result = Results.TODO

  def updateCartProduct(id: String, qty: String): Result = Results.TODO

  def addProduct(): Result = Results.TODO
}
