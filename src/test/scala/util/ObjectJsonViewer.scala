package util

import org.scalatest.{ShouldMatchers, FunSpec}
import org.json4s._
import org.json4s.native
import native.Serialization._
import org.json4s.native.Serialization._
import sample.Transfer.TransferRequest
import sample.Account

class ObjectJsonViewer extends FunSpec with ShouldMatchers {
  implicit def json4sFormats: Formats = DefaultFormats

  describe("ObjectJsonViewer"){
    it("is just used to show json result of an scala object") {
      println(write(TransferRequest(1, Account("yy", 100.0), Account("xx", 100.0), 50.0)))
    }
  }
}
