name := "akka-streams-rabbitmq"

version := "1.0"

scalaVersion := "2.12.3"

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "sonatype-public" at "https://oss.sonatype.org/content/groups/public"
)

libraryDependencies ++= Seq (
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test,
  "com.typesafe.play" % "play-json_2.12" % "2.6.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.lightbend.play" %% "play-socket-io" % "1.0.0-beta-2",
  "com.softwaremill.macwire" %% "macros" % "2.3.0" % Provided,
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "0.11",
  "com.github.javafaker" % "javafaker" % "0.13"
)


