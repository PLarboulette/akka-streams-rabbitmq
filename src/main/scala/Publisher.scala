
import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.alpakka.amqp.scaladsl.AmqpSink
import akka.stream.alpakka.amqp.{AmqpConnectionUri, AmqpSinkSettings, QueueDeclaration}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Publisher extends App {

  implicit val system = ActorSystem("Akka-Streams-RabbitMQ-Publisher")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = materializer.executionContext

  val host :  String = "127.0.0.1"
  val port : Int = 32769
  val connectionsStrings = AmqpConnectionUri(s"amqp://$host:$port")

  val queue = "test"

  val publisher = Publisher(connectionsStrings, queue)

  val input = 0 to 50

  publisher.publish(input)
    .onComplete {
      _ =>
        println("Messages published ...")
        system.terminate()
    }
}

case class Publisher(connectionStrings : AmqpConnectionUri, queue : String) {

  def publish (input : Any) (implicit ec : ExecutionContext, mat : Materializer) : Future[Either[String,Done]] = {
    try {
      val queueDeclaration = QueueDeclaration(queue)

      val amqpSink = AmqpSink.simple(
        AmqpSinkSettings(connectionStrings).withRoutingKey(queue).withDeclarations(queueDeclaration)
      )

      val source = Helper.inputToSource(input)
      val convertedSource = Helper.anyToByteString(source)

      convertedSource
        .runWith(amqpSink)
        .map (done =>Right(done))

    } catch {
      case e : Exception =>
        println(e.getMessage)
        Future.successful(Left(e.getMessage))
    }
  }
}
