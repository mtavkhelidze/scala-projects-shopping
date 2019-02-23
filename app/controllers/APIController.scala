package controllers

import dao.{ CartDao, ProductDao }
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.swagger.annotations._
import javax.inject.{ Inject, Singleton }
import models.{ Cart, Product }
import play.api.libs.circe.Circe
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
@Api(value = "Product and Cart API")
class APIController @Inject()(
    cc: ControllerComponents,
    products: ProductDao,
    cart: CartDao,
    implicit val ec: ExecutionContext
) extends AbstractController(cc) with Circe {

  private val recover: PartialFunction[Throwable, Result] = {
    case e: Throwable => {
      Logger.error(s"Database error: $e")
      InternalServerError("Database error")
    }
  }

  @ApiOperation(value = "List all products")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "The list of products")
    )
  )
  def listProducts(): Action[AnyContent] = Action.async { _ =>
    for {
      ps <- products.all()
    } yield Ok(ps.asJson)
  }

  @ApiOperation(value = "Add a product", consumes = "text/plain")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "The product to add",
        required = true,
        dataType = "models.Product",
        paramType = "body"
      )
    )
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Product added"),
      new ApiResponse(code = 400, message = "Invalid body supplied"),
      new ApiResponse(code = 500, message = "Database error")
    )
  )
  def addProduct(): Action[AnyContent] = Action.async { req =>
    val maybeProduct = decode[Product](req.body.asText.getOrElse(""))
    maybeProduct match {
      case Right(product) =>
        Logger.info(s"Adding product: $product")
        products.insert(product).map(_ => OK).recover(recover)
      case Left(e) =>
        Logger.error(s"Error while adding product: $e")
        Future.successful(BadRequest)
    }
    Future.successful(Ok)
  }

  def listCartProducts(): Action[AnyContent] = Action.async { req =>
    val userFuture: Option[String] = req.session.get("user")
    userFuture match {
      case Some(user) => {
        Logger.info(s"User '$user' listing products in cart")
        cart.cartFor(user).map(products => Ok(products.asJson)).recover(recover)
      }
      case None => Future.successful(BadRequest)
    }
  }

  def addCartProduct(id: String, qty: String): Action[AnyContent] =
    Action.async { req =>
      val userFuture: Option[String] = req.session.get("user")
      userFuture match {
        case Some(user) => {
          Logger.info(s"User '$user' inserting $id:$qty in cart")
          cart.insert(Cart(user, id, qty.toInt)).recover(recover).map(_ => Ok)
        }
        case None => Future.successful(BadRequest)
      }
    }

  def updateCartProduct(id: String, qty: String): Action[AnyContent] =
    Action.async { req =>
      val userFuture = req.session.get("user")
      userFuture match {
        case Some(user) => {
          Logger.info(s"User '$user' updating $id to $qty in cart")
          cart.update(Cart(user, id, qty.toInt)).recover(recover).map(_ => Ok)
        }
        case None => Future.successful(BadRequest)
      }
    }

  def deleteCartProduct(id: String): Action[AnyContent] = Action.async { req =>
    val userFuture = req.session.get("user")
    userFuture match {
      case Some(user) => {
        Logger.info(s"User '$user' deleting $id from cart")
        cart.remove(user, id).recover(recover).map(_ => Ok)
      }
      case None => Future.successful(BadRequest)
    }
  }

  @ApiOperation(value = "Login to the service", consumes = "text/plain")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "Create a session for a user",
        required = true,
        dataType = "java.lang.String",
        paramType = "body"
      )
    )
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Login successful"),
      new ApiResponse(code = 400, message = "Invalid user name")
    )
  )
  def login(): Action[AnyContent] = Action { req =>
    req.body.asText match {
      case Some(user) => Ok.withSession("user" -> user)
      case None => BadRequest
    }
  }
}
