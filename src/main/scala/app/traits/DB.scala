package app.traits

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import slick.driver.H2Driver.api._

trait DB {
  implicit val db = Database.forConfig("h2mem1")
}

trait ActorApp {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
}

