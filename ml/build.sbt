

name := "machine learning"

version := "1.0"

//scalaVersion := "2.11.8"

fork in run := true
javaOptions in run ++= Seq(
	    "-Dlog4j.debug=true",
	    "-Dlog4j.configuration=log4j.properties")

scalaSource in Compile <<= (sourceDirectory in Compile) (_ /"scala")
mainClass in (Compile, run) in ThisBuild := Some("Mainlearning.scala")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" %% "spark-sql" % "2.1.0",
  "org.apache.spark"  %% "spark-mllib" % "2.1.0")
  
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4"

