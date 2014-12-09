package sample

import akka.actor.ActorSystem
import org.json4s.{DefaultFormats, Formats}
import sample.util.JSONUtil
import spray.client.pipelining._
import spray.http.HttpRequest
import spray.httpx.Json4sSupport

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object MyJsonProtocol extends Json4sSupport {
  override implicit def json4sFormats: Formats = DefaultFormats

  case class TransferRequest(id: Int, from: Account, to: Account, amount: Double)
  case class TransferSuccess(id: Int)
  case class Account(owner: String, var balance: Double)
}

object SampleClient extends App {
  implicit val system = ActorSystem()
  import sample.SampleClient.system.dispatcher
  implicit def json4sFormats: Formats = JSONUtil.formats
  import sample.MyJsonProtocol._

  val pipeline: HttpRequest => Future[TransferSuccess] = sendReceive ~> unmarshal[TransferSuccess]

  val rs = Await.result(pipeline(Post("http://127.0.0.1:8080/account/transaction",
    TransferRequest(1, Account("yy",500.0), Account("zz", 100.0), 200.0))),
    1 seconds)
  println(s"received: $rs")
  system.shutdown()
}
