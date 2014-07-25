import akka.actor.Actor
import org.json4s.Formats
import sample.Transfer.{TransferFailed, TransferRequest, TransferSuccess}
import sample.util.JSONUtil
import sample.{Person, Transfer}
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.Json4sSupport
import spray.httpx.encoding.Gzip
import spray.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService with Json4sSupport {
  implicit def json4sFormats: Formats = JSONUtil.formats
  val getDetachComplete = get & detach() & complete

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to
                  <i>spray-routing</i>
                  on
                  <i>spray-can</i>
                  !</h1>
              </body>
            </html>
          }
        }
      }
    } ~
      path("account" / "transaction") {
        post {
          entity(as[TransferRequest]) { transferReq =>
            detach() {
              complete {
                Transfer.transfer(transferReq) match {
                  case rs: TransferSuccess => rs
                  case rs: TransferFailed => StatusCodes.BadRequest -> rs
                }
              }
            }
          }
        }
      } ~
      path("account" / "transaction.gz") {
        post {
          entity(as[TransferRequest]) { transferReq =>
            detach() {
              compressResponse(Gzip) {
                complete {
                  Transfer.transfer(transferReq)
                }
              }
            }
          }
        }
      } ~
      path("person") {
        getDetachComplete {
          println("receiving request /person")
          val p = new Person
          p.name = "notyy"
          p.age = 37
          p
        }
      } ~
      path("person1") {
        get {
          detach() {
            complete {
              println("receiving request /person1")
              val p = new Person
              p.name = "notyy1"
              p.age = 37
              p
            }
          }
        }
      }

}