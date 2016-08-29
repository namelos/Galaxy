package galaxy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol

trait ActorApp {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
}

case class Article(title: String, content: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat2(Article)
}

object Main extends App with ActorApp with JsonSupport {
  val article = Article("my article", "this is my first article")

  val route: Route =
    get {
      pathSingleSlash { complete("Hello") }
      path("api") { complete(article) }
    } ~
    post {
      entity(as[Article]) { article =>
        val title = article.title
        val content = article.content

        complete(s"title -> $title, content -> $content")
      }
    }

  Http().bindAndHandle(route, "localhost", 3000)
}
