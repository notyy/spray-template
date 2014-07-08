package sample.util

import org.json4s._
import org.json4s.native.Serialization
import sample.Person
import scala.util.Try

object JSONUtil {

  implicit val formats = DefaultFormats + FieldSerializer[Person]()

  def toJSON(objectToWrite: AnyRef): String = Serialization.write(objectToWrite)

  def fromJSONOption[T](jsonString: String)(implicit mf: Manifest[T]): Option[T] = Try(Serialization.read(jsonString)).toOption

}
