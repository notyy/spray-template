import org.scalatest.{MustMatchers, ShouldMatchers, FunSpec}
import sample.{Person, Account}
import sample.Transfer.{TransferFailed, TransferSuccess, TransferRequest}
import spray.httpx.encoding.Gzip
import spray.testkit.ScalatestRouteTest
import scala.concurrent.duration._

class MyServiceTest extends FunSpec with ScalatestRouteTest with MyService with ShouldMatchers {
  implicit val routeTestTimeout = RouteTestTimeout(15.seconds)

  def actorRefFactory = system

  describe("MyService") {
    describe("POST /account/transaction") {
      it("should transfer money using parameters defined in transferRequest") {
        val transferRequest = TransferRequest(1, Account("xx", 100.0), Account("yy", 100.0), 50.0)
        Post("/account/transaction", transferRequest) ~> myRoute ~> check {
          status.intValue should be (200)
          println(body.asString)
          val rs = responseAs[TransferSuccess]
          rs.id should be (1)
        }
      }
      it("should not allow transfer more money than source account's balance"){
        val transferRequest = TransferRequest(1, Account("xx", 100.0), Account("yy", 100.0), 150.0)
        Post("/account/transaction", transferRequest) ~> myRoute ~> check {
          status.intValue should be (400) //400 Bad Request
          println(body.asString)
          val rs = responseAs[TransferFailed]
          rs.id should be (1)
          rs.message should be ("not enough balance")
        }
      }
      it("should response with gz if required") {
        val transferRequest = TransferRequest(1, Account("xx", 100.0), Account("yy", 100.0), 50.0)
        Post("/account/transaction.gz", transferRequest) ~> myRoute ~> check {
          status.intValue should be (200)
          header("Content-Encoding").map(_.toString()) shouldBe Some("Content-Encoding: gzip")
//          val rs = responseAs[TransferSuccess]
//          rs.id should be (1)
        }
      }
      it("should get json from normal class instead of case class"){
        Get("/person") ~> myRoute ~> check {
          println(body.asString)
          val rs = responseAs[Person]
          rs.name shouldBe "notyy"
          rs.age shouldBe 37
        }
      }
    }
  }
}
