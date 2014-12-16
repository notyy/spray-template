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

import scala.io.Source

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService with ResourceService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute ~ resourceRoute)
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
      } ~
      path("javascript") {
        get {
          //          respondWithMediaType(`text/plain`) {
          complete {
            HttpResponse(StatusCodes.OK, HttpEntity("xxxx"))
          }
          //          }
        }
      }
}

trait ResourceService extends HttpService {
  def resourceRoute =
    path("file") {
      get {
        respondWithMediaType(`text/csv`) {
          respondWithHeader(HttpHeaders.`Content-Disposition`("attachment", Map("filename" -> "myFile.csv"))) {
            complete("hello,world")
          }
        }
      }
    } ~
      path("stream") {
        get {
          respondWithMediaType(`text/html`) {
            complete {
              simpleStringStream
            }
          }
        }
      } ~
      path("stringStream") {
        get {
          complete{
            stringStream
          }
        }
      } ~
      clientIP { ip =>
        path("ip") {
          get {
            complete {
              s"client ip is $ip"
            }
          }
        }
      }

  //this streaming example is copied from offical spray example:
  //https://github.com/spray/spray/blob/release/1.2/examples/spray-routing/on-spray-can/src/main/scala/spray/examples/DemoService.scala
  // we prepend 2048 "empty" bytes to push the browser to immediately start displaying the incoming chunks
  lazy val streamStart = " " * 2048 + "<html><body><h2>A streaming response</h2><p>(for 15 seconds)<ul>"
  lazy val streamEnd = "</ul><p>Finished.</p></body></html>"

  def simpleStringStream: Stream[String] = {
    //be careful!! continually is infinite
    val secondStream = Stream.continually {
      // CAUTION: we block here to delay the stream generation for you to be able to follow it in your browser,
      // this is only done for the purpose of this demo, blocking in actor code should otherwise be avoided
      Thread.sleep(500)
      "<li>" + DateTime.now.toIsoDateTimeString + "</li>"
    }
    streamStart #:: secondStream.take(15) #::: streamEnd #:: Stream.empty
  }

  def iterToStream(iter: Iterator[String]): Stream[String] = {
    if(iter.hasNext){
      val lines: String = iter.take(2).mkString("\n") + "\n"
      Thread.sleep(500)
      Stream.cons(lines,  iterToStream(iter))
    }else{
      Stream.empty
    }
  }

  def stringStream: Stream[String] = {
    val iter = Source.fromFile("src/test/resources/log4j.properties").getLines()
//    iterToStream(iter)
    iter.grouped(10).map(_.mkString("\n")+"\n").toStream
  }
}


