package app.controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import app.rep.ArticleRep.{Article, ArticleSerializer, all, create, insert, insertSamples}
import app.traits.ActorApp

object Application extends App with ArticleSerializer with ActorApp {
  Await.result(
    create
      .flatMap(_ => insertSamples)
      .flatMap(_ => insert(Article(null, "special", "hidden article")))
      .flatMap(_ => all)
      .map(articles => articles.foreach(println)), Inf)

  def getArticle(n: Int): Future[Option[Article]] =
    all.map(articles => Option(articles(n)))

  val route: Route =
    get {
      pathSingleSlash {
        complete(getArticle(2))
      }
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
