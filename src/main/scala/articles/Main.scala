package articles

import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape
import app.traits.DB
import slick.lifted.{Query, TableQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class Article(id: Int, title: String, content: String)

class ArticleTable(tag: Tag) extends Table[(Int, String, String)](tag, "ARTICLES") {
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey)
  def title: Rep[String] = column[String]("TITLE")
  def content: Rep[String] = column[String]("CONTENT")
  def * : ProvenShape[(Int, String, String)] = (id, title, content)
}

object Main extends App {
  implicit val db = Database.forConfig("h2mem1")

  val articles = TableQuery[ArticleTable]

//  val setupAction = DBIO.seq(
//    articles.schema.create,
//
//    articles += (1, "title1", "content1"),
//    articles += (2, "title2", "content2"),
//    articles += (3, "title3", "content3")
//  )
//
//  val setupFuture = db.run(setupFuture)
//
//  val f

//  try {
    val f = db.run(
      DBIO.seq(
        articles.schema.create,
        articles += (1, "title", "content")
      )
    ).flatMap { _ =>
      db.run(articles.result).map { articles =>
        articles.foreach(println)
      }
    }

    Await.result(f, Duration.Inf)
//  } finally db.close()

}
