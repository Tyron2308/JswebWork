name := "scalaServer"

version := "1.0"

scalaVersion := "2.11.8"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }


libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "com.outworkers" %% "phantom-dsl" % "2.11.2" exclude("org.slf4j" ,"log4j-over-slf4j")
libraryDependencies += "com.outworkers" %% "phantom-connectors" % "2.11.2" exclude("org.slf4j" ,"log4j-over-slf4j")
libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full exclude("org.slf4j", "log4j-over-slf4j"))
//libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6" exclude("org.slf4j" ,"log4j-over-slf4j")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4"
