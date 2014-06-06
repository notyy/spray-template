import org.scalatest.{ShouldMatchers, FunSpec}
import sample.Account
import sample.Transfer.{TransferFailed, TransferSuccess, TransferRequest}
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
    }
    //    it("should return a greeting for GET requests to the root path") {
    //      Get() ~> myRoute ~> check {
    //        responseAs[String].contains("Say hello") should be (true)
    //      }
    //    }
    //
    //    it("should leave GET requests to other paths unhandled") {
    //      Get("/kermit") ~> myRoute ~> check {
    //        handled should be (false)
    //      }
    //    }
    //
    //    it("should return a MethodNotAllowed error for PUT requests to the root path") {
    //      Put() ~> sealRoute(myRoute) ~> check {
    //        status should be (MethodNotAllowed)
    //        responseAs[String] should be("HTTP method not allowed, supported methods: GET")
    //      }
    //    }
  }
}
