package controllers

import dao.ProductDao
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import models.Product
import play.api.libs.circe.Circe
import play.api.mvc._
import play.api.Logger
import play.mvc.{ Result, Results }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

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

  def addProduct(): Action[AnyContent] = Action.async { req =>
    val maybeProduct = decode[Product](req.body.asText.getOrElse(""))
    maybeProduct match {
      case Right(product) => {
        val insert = products.insert(product).recover {
          case e => {
            Logger.error(s"Error writing database: $e")
            InternalServerError("Cannot write in the database")
          }
        }
        insert.map(_ => OK)
      }
      case Left(e) => {
        Logger.error(s"Error while adding product: $e")
        Future.successful(BadRequest)
      }
    }
    Future.successful(Ok)
  }

  def listCartProducts(): Result = Results.TODO

  def deleteCartProduct(id: String): Result = Results.TODO

  def addCartProduct(id: String, qty: String): Result = Results.TODO

  def updateCartProduct(id: String, qty: String): Result = Results.TODO
}
