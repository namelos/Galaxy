package app.controllers

import app.rep.ArticleRep
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends App {
  ArticleRep.create()
    .flatMap { _ => ArticleRep.insertSamples() }
    .flatMap { _ => ArticleRep.all() }
    .map{ articles => articles.foreach(article => println(article)) }
}
