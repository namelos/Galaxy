package DB
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

// tables

class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
  def id: Rep[Int] = column[Int]("SUP_ID", O.PrimaryKey)
  def name: Rep[String] = column[String]("SUP_NAME")
  def street: Rep[String] = column[String]("STREET")
  def city: Rep[String] = column[String]("CITY")
  def state: Rep[String] = column[String]("STATE")
  def zip: Rep[String] = column[String]("ZIP")

  def * : ProvenShape[(Int, String, String, String, String, String)] = (id, name, street, city, state, zip)
}

class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
  def name: Rep[String] = column[String]("COF_NAME", O.PrimaryKey)
  def supID: Rep[Int] = column[Int]("SUP_ID")
  def price: Rep[Double] = column[Double]("PRICE")
  def sales: Rep[Int] = column[Int]("SALES")
  def total: Rep[Int] = column[Int]("TOTAL")

  def * : ProvenShape[(String, Int, Double, Int, Int)] = (name, supID, price, sales, total)

  def supplier: ForeignKeyQuery[Suppliers, (Int, String, String, String, String, String)] = foreignKey("SUP_FK", supID, TableQuery[Suppliers])(_.id)
}

// app

object Main extends App {
  val db = Database.forConfig("h2mem1")

  try {
    val suppliers: TableQuery[Suppliers] = TableQuery[Suppliers]
    val coffees: TableQuery[Coffees] = TableQuery[Coffees]

    val setupAction: DBIO[Unit] = DBIO.seq(
      (suppliers.schema ++ coffees.schema).create,

      suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
      suppliers += ( 49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
      suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")
    )

    val setupFuture: Future[Unit] = db.run(setupAction)

    val f = setupFuture.flatMap { _ =>

      val insertAction: DBIO[Option[Int]] = coffees ++= Seq(
        ("Colombian",         101, 7.99, 0, 0),
        ("French_Roast",       49, 8.99, 0, 0),
        ("Espresso",          150, 9.99, 0, 0),
        ("Colombian_Decaf",   101, 8.99, 0, 0),
        ("French_Roast_Decaf", 49, 9.99, 0, 0)
      )

      val insertAndPrintAction: DBIO[Unit] = insertAction.map { coffeesInsertResult =>
        coffeesInsertResult foreach { numRows =>
          println(s"Inserted $numRows rows into the Coffees table")
        }
      }

      val allSuppliersAction: DBIO[Seq[(Int, String, String, String, String, String)]] =
        suppliers.result

      val combinedAction: DBIO[Seq[(Int, String, String, String, String, String)]] =
        insertAndPrintAction >> allSuppliersAction

      val combinedFuture: Future[Seq[(Int, String, String, String, String, String)]] =
        db.run(combinedAction)

      combinedFuture.map { allSuppliers =>
        allSuppliers.foreach(println)
      }
    }.flatMap { _ =>
      val coffeeNamesAction: StreamingDBIO[Seq[String], String] = coffees.map(_.name).result
      val coffeeNamesPublisher: DatabasePublisher[String] = db.stream(coffeeNamesAction)

      coffeeNamesPublisher.foreach(println)
    }.flatMap { _ =>
      val filterQuery: Query[Coffees, (String, Int, Double, Int, Int), Seq] = coffees.filter(_.price > 9.0)
      println("Generated SQL for filter query:\n" + filterQuery.result.statements)
      db.run(filterQuery.result.map(println))
    }.flatMap { _ =>
      val updateQuery: Query[Rep[Int], Int, Seq] = coffees.map(_.sales)
      val updateAction: DBIO[Int] = updateQuery.update(1)
      println("Generated SQL for update query:\n" + updateQuery.updateStatement)
      db.run(updateAction.map { numUpdatedRows => println(s"Updated $numUpdatedRows") })
    }.flatMap { _ =>
      val deleteQuery: Query[Coffees, (String, Int, Double, Int, Int), Seq] = coffees.filter(_.price < 8.0)
      val deleteAction = deleteQuery.delete
      println("Generated SQL for Coffees delete:\n" + deleteAction.statements)
      db.run(deleteAction.map { numDeleteRows => println(s"Deleted $numDeleteRows rows") })
    }.flatMap { _ =>
      val sortByPriceQuery: Query[Coffees, (String, Int, Double, Int, Int), Seq] = coffees.sortBy(_.price)
      println("Generated SQL for sorted by price:\n" + sortByPriceQuery.result.statements)
      db.run(sortByPriceQuery.result).map(println)
    }.flatMap { _ =>
      val composedQuery: Query[Rep[String], String, Seq] = coffees.sortBy(_.name).take(3).filter(_.price > 9.0).map(_.name)
      println("Generated SQL for composed query:\n" + composedQuery.result.statements)
      db.run(composedQuery.result).map(println)
    }.flatMap { _ =>
      val joinQuery: Query[(Rep[String], Rep[String]), (String, String), Seq] = for {
        c <- coffees if c.price > 9.0
        s <- c.supplier
      } yield (c.name, s.name)

      println("Generated SQL for the join query:\n" + joinQuery.result.statements)
      db.run(joinQuery.result).map(println)
    }.flatMap { _ =>
      val maxPriceColumn: Rep[Option[Double]] = coffees.map(_.price).max
      println("Generated SQL for max price column:\n" + maxPriceColumn.result.statements)
      db.run(maxPriceColumn.result).map(println)
    }.flatMap { _ =>
      val state = "CA"
      val plainQuery = sql"select SUP_NAME from SUPPLIERS where STATE = $state".as[String]
      println("Generated SQL for plain query:\n" + plainQuery.statements)
      db.run(plainQuery).map(println)
    }

    Await.result(f, Duration.Inf)

  } finally db.close
}
