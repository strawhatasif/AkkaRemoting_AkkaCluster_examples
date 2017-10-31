name := """AkkaRemotingExamples"""

version := "1.0"

scalaVersion := "2.11.11"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.5.6"

libraryDependencies +=
  "com.typesafe.akka" % "akka-remote_2.11" % "2.5.6"
