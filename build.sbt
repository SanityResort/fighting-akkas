organization := "de.sanityresort"

name := "fight"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= List(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.11",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.akka" % "akka-http-core-experimental_2.11" % "1.0-RC4",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "1.0-RC4",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-RC4",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "1.0-RC4",
  "io.spray" %% "spray-json" % "1.3.1",
  "de.heikoseeberger" %% "akka-sse" % "0.14.0"

)

resolvers += "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"