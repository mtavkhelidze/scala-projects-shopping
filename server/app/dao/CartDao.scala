package dao

import ge.zgharbi.shopping.shared.Cart
import javax.inject._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class CartDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    (
        implicit executionContext: ExecutionContext
    ) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Future[Seq[Cart]] = db.run(carts.result)

  def insert(cart: Cart): Future[Int] = db.run(carts += cart)

  def cartFor(user: String): Future[Seq[Cart]] =
    db.run(carts.filter(_.user === user).result)

  def remove(user: String, productCode: String): Future[Int] =
    db.run(carts.filter(t => matchKey(t, user, productCode)).delete)

  def update(cart: Cart): Future[Int] = {
    val q = for {
      c <- carts if matchKey(c, cart.user, cart.productCode)
    } yield c.quantity
    db.run(q.update(cart.quantity))
  }

  private def matchKey(
      table: CartTable, user: String, productCode: String
  ): Rep[Boolean] = {
    table.user === user && table.productCode === productCode
  }

  private class CartTable(tag: Tag) extends Table[Cart](tag, "CART") {

    def user = column[String]("USER")

    def productCode = column[String]("CODE")

    def quantity = column[Int]("QTY")

    override def * = (user, productCode, quantity) <>
        (Cart.tupled, Cart.unapply)
  }

  private val carts = TableQuery[CartTable]
}
