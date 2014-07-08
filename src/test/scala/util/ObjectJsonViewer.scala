package util

import org.json4s._
import org.json4s.native.Serialization._
import org.scalatest.{FunSpec, ShouldMatchers}
import sample.Transfer.TransferRequest
import sample.util.JSONUtil
import sample.{Account, Person}

class ObjectJsonViewer extends FunSpec with ShouldMatchers {
  implicit def json4sFormats: Formats = JSONUtil.formats

  describe("ObjectJsonViewer"){
    it("is just used to show json result of an scala object") {
      println(write(TransferRequest(1, Account("yy", 100.0), Account("xx", 100.0), 50.0)))
    }
    it("can transform normal class to json") {
      val p = new Person
      p.name = "notyy"
      p.age = 37
      println(write(p))
    }
  }
}
