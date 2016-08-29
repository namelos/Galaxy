package app.traits

import slick.driver.H2Driver.api._

trait DB {
  implicit val db = Database.forConfig("h2mem1")
}

