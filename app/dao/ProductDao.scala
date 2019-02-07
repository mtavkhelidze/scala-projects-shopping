package dao

import javax.inject._
import models.Product
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class ProductDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Future[Seq[Product]] = db.run(products.result)

  def insert(product: Product): Future[Unit] =
    db.run(products insertOrUpdate product).map { _ => () }

  private class ProductsTable(tag: Tag) extends Table[Product](tag, "PRODUCTS") {
    def name = column[String]("NAME")

    def code = column[String]("CODE")

    def description = column[String]("DESCRIPTION")

    def price = column[Double]("PRICE")

    override def * = (name, code, description, price) <>
        (Product.tupled, Product.unapply)
  }

  private val products = TableQuery[ProductsTable]
}
