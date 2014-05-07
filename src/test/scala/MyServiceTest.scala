import org.scalatest.{ShouldMatchers, FunSpec}
import spray.testkit.ScalatestRouteTest
import spray.http._
import StatusCodes._

class MyServiceTest extends FunSpec with ScalatestRouteTest with MyService with ShouldMatchers {
  def actorRefFactory = system

  describe("MyService") {
    it("should return a greeting for GET requests to the root path") {
      Get() ~> myRoute ~> check {
        responseAs[String].contains("Say hello") should be (true)
      }
    }

    it("should leave GET requests to other paths unhandled") {
      Get("/kermit") ~> myRoute ~> check {
        handled should be (false)
      }
    }

    it("should return a MethodNotAllowed error for PUT requests to the root path") {
      Put() ~> sealRoute(myRoute) ~> check {
        status should be (MethodNotAllowed)
        responseAs[String] should be("HTTP method not allowed, supported methods: GET")
      }
    }
  }
}
