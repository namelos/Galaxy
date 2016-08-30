package app.rep

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape
import app.traits.DB
import slick.lifted.TableQuery
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object ArticleRep extends DB {
  val articles = TableQuery[ArticleTable]

  def create: Future[Unit] = db.run(articles.schema.create)

  def all: Future[Seq[Article]] = db.run(articles.result)

  def insert(article: Article): Future[Unit] = db.run(articles += article).map { _ => () }

  def insertSamples: Future[Unit] =
    db.run(DBIO.seq(
      articles += Article(Some(1), "title1", "content1"),
      articles += Article(Some(2), "title2", "content2"),
      articles += Article(Some(3), "title3", "content3")
    ))

  trait ArticleSerializer extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val itemFormat = jsonFormat3(Article)
  }

  case class Article(id: Option[Int], title: String, content: String)

  class ArticleTable(tag: Tag) extends Table[Article](tag, "ARTICLES") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def title: Rep[String] = column[String]("TITLE")
    def content: Rep[String] = column[String]("CONTENT")
    def * : ProvenShape[Article] = (id.?, title, content) <> (Article.tupled, Article.unapply)
  }
}
