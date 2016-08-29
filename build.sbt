name := "Galaxy"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.9",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.9",
  "com.typesafe.akka" %% "akka-stream" % "2.4.9",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.9",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.9",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.9",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.h2database" % "h2" % "1.4.187",
  "org.slf4j" % "slf4j-nop" % "1.7.10",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)