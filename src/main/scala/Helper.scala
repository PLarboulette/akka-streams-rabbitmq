import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object Helper {

  def inputToSource (input : Any) (implicit ec : ExecutionContext) : Source[Any, NotUsed] = {
    input match {
      case i : Int => Source.single(i)
      case bool : Boolean => Source.single(bool)
      case str: String => Source.single(str)
      case futureItem : Future[_] => Source.fromFuture(futureItem)
      case list : List[_] => Source(list)
      case range : Range => Source(range)
      case futureItems : Future[List[_]] => Source.fromFuture(futureItems)
      case _ => Source.empty
    }
  }

  def anyToByteString (source : Source[Any, NotUsed]) (implicit ec : ExecutionContext) : Source[ByteString, NotUsed] = {
    source.map {
      case s : String => ByteString(s)
      case i : Int => ByteString(i.toString)
      case _ => ByteString("OMG, what's that ? ")
    }
  }
}
