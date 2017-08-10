import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.amqp.scaladsl.AmqpSource
import akka.stream.alpakka.amqp.{AmqpConnectionUri, NamedQueueSourceSettings, QueueDeclaration}
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult, Materializer, ThrottleMode}
import akka.util.ByteString

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Consumer extends  App {

  val host :  String = "127.0.0.1"
  val port : Int = 32769
  val connectionsStrings = AmqpConnectionUri(s"amqp://$host:$port")

  val queue = "test"

  val consumer = Consumer(connectionsStrings, queue)

  consumer.consume(writeToFile = true, Some("test.txt"))

}

case class Consumer(connectionStrings : AmqpConnectionUri, queue : String) {

  def consume (writeToFile : Boolean, filepath : Option[String] = None) : Unit = {

    implicit val system = ActorSystem("Akka-Streams-RabbitMQ-Consumer")
    implicit val materializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = materializer.executionContext

    try {
      val queueDeclaration = QueueDeclaration(queue)

      val source =
        AmqpSource(
          NamedQueueSourceSettings(connectionStrings, queue).withDeclarations(queueDeclaration), bufferSize = 10)
          .map(_.bytes.utf8String)
          .take(500)

      if (writeToFile) {
        writeFile(source, filepath.getOrElse(""))
            .map(_ => println("Done"))
            .onComplete(_ => system.terminate())
      } else {
        source
          .throttle(elements = 1, per = 2.second, maximumBurst = 0, ThrottleMode.shaping)
          .runForeach(println)
          .map(_ => println("Done"))
          .onComplete(_ => system.terminate())
      }

    } catch {
      case e : Exception =>
        println(e.getMessage)
    }
  }

  def writeFile (source : Source[Any, NotUsed], filepath : String) (implicit ec : ExecutionContext, mat : Materializer) : Future[Either[String, IOResult]] = {
    try {
      source
        .throttle(elements = 1, per = 2.second, maximumBurst = 0, ThrottleMode.shaping)
        .map(item => ByteString(s"$item : ${System.currentTimeMillis()} \n"))
        .runWith(FileIO.toPath(Paths.get(filepath)))
        .map(item => Right(item))
    } catch {
      case e : Exception =>
        println(e.getMessage)
        Future.successful(Left(e.getMessage))
    }
  }
}
