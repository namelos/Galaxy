package streamExample

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.Future

trait StreamApp {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
}

object Main extends App with StreamApp {
  val source = Source(1 to 10)
  
  source.runForeach(println)
}
