package dao

import models.Product
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

class ProductDaoSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {
  "dao.ProductDao" should {
    "Have default rows on database creation" in {
      val app2Dao = Application.instanceCache[ProductDao]
      val dao: ProductDao = app2Dao(app)
      val expected = Set(
        Product("pepper", "ald2", "Pepper is a robot on wheels with LCD screen.", 7000),
        Product("nao", "ald1", "NAO is a humanoid robot.", 3500),
        Product("beobot", "beo1", "Beobot is a multipurpose robot.", 159.0),
      )
      dao.all().futureValue should contain theSameElementsAs expected
    }
  }
}
