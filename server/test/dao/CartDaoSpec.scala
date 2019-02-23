package dao

import ge.zgharbi.shopping.shared.Cart
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers._
import org.scalatest.RecoverMethods._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CartDaoSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {
  "dao.CartDao" should {
    val app2Dao = Application.instanceCache[dao.CartDao]

    "be empty on database creation" in {
      val dao: CartDao = app2Dao(app)
      dao.all().futureValue shouldBe empty
    }

    "accept a cart to add" in {
      val dao: CartDao = app2Dao(app)
      val user = "userAdd"

      val expected = Set(
        Cart(user, "ald1", 1),
        Cart(user, "be01", 5)
      )

      val noise = Set(
        Cart("userNoise", "ald2", 10)
      )

      val allCarts = expected ++ noise

      val insertFutures = allCarts.map(dao.insert)

      whenReady(Future.sequence(insertFutures)) { _ =>
        dao.cartFor(user).futureValue should contain theSameElementsAs expected
        dao.all().futureValue.size should equal(allCarts.size)
      }
    }

    "trow an error on attempt to insert with existing user and product code pair" in {
      val dao: CartDao = app2Dao(app)
      val user = "userAdd"
      val expected = Set(
        Cart(user, "ald1", 1),
        Cart(user, "be01", 5)
      )
      val noise = Set(
        Cart(user, "ald1", 10),
      )
      val allCarts = expected ++ noise

      val futures = allCarts.map(dao.insert)
      recoverToSucceededIf[org.h2.jdbc.JdbcSQLException] {
        Future.sequence(futures)
      }
    }

    "remove products from a cart" in {
      val dao: CartDao = app2Dao(app)
      val user = "userRemove"
      val initial = Set(
        Cart(user, "ald1", 1),
        Cart(user, "be01", 5)
      )
      val expected = Vector(Cart(user, "ald1", 1))

      whenReady(Future.sequence(initial.map(dao.insert))) { _ =>
        dao.remove(user, "be01").futureValue
        dao.cartFor(user).futureValue should contain theSameElementsAs expected
      }
    }

    "accept to update quantities of an item in a cart" in {
      val dao: CartDao = app2Dao(app)
      val user = "userUpd"
      val initial = Vector(Cart(user, "ALD1", 1))
      val expected = Vector(Cart(user, "ALD1", 5))

      whenReady(Future.sequence(initial.map(dao.insert))) { _ =>
        dao.update(Cart(user, "ALD1", 5)).futureValue
        dao.cartFor(user).futureValue should contain theSameElementsAs expected
      }
    }
  }
}
