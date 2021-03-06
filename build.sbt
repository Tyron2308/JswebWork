import scala.sys.process.Process
import complete.DefaultParsers._


/*** sbt root avec plusieurs submodules - ml - scalaServer - common
  *
  *  pour compiler le tout faire depuis le racine du repot : sbt - compile - run
  *  pour creer un fat jar : assembly - puis aller dans le target a la racine du repot
  *  git son code depuis sbt : run sbt - push : task pour push directement mais ca marche
  *  qu'avec mon repo pour l'instant, faut changer le script
  *
  *  pour effecter une action sur un submodule faire : submodule/run ou submodule/compile ou submodule/package etc..
  *  apres avoir compile le sbt principal (recommande sauf si pas besoin de dependance d autres modules)
  */

name := "RecomanderSystemJSWEB"

scalacOptions in Test ++= Seq("-Yrangepos")

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

fork in run := true

mainClass in (Compile, run) := Some("Main")
mainClass in assembly := Some("Main")

//assemblySettings /*** find out whky this is not working ***/

lazy val push = inputKey[Unit]("test")
push := { val s: TaskStreams = streams.value
      val args: Seq[String] = spaceDelimited("<args>").parsed
      println("the argument for push script are : " + args.mkString(" -- "))
      val shell : Seq[String] = Seq("bash", "-c")
      if (args.length < 3) {
	      println("this task must be called with 3 arguments")
        println("1 : message of commit")
        println("2 : branch of commit")
        println("1 : if you want to pull first else 0")
	      throw new IllegalStateException("pushOnGit");
      }
      shell :+  "./gitscript.sh " + args{0} + " " +  args{1} + " " + args{2} !
}

lazy val Helpme = taskKey[Unit]("help usage")
Helpme := {
  println("push + <args>: permet de push sur le repos git DemoCassandra")
  println("compile : permet de compiler tout les submodule defini par ce build")
  println("pour effectuer une action sur un submodule en particulier utiliser : submodule/run")
  println("next to come, tchuss")
}

val settingTest :Seq[ModuleID] =
  Seq("org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.11.5" % "test")

lazy val commonsettings = Seq(
  organization := "com.JSWB",
  version  := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.8" )

assemblyShadeRules in assembly :=
  Seq(ShadeRule.rename("com.google.**" -> "shadeio.@1").inAll)


//assemblyShadeRules in assembly := Seq(
//  ShadeRule.rename("com.google.guava.**" -> "my_conf.@1")
//    .inLibrary("com.google.guava" % "config" % "11.0")
//    .inProject
//)


/*** ***/
val sequence_common = Seq("org.specs2" %% "specs2-core" % "3.9.1" % "test",
    "junit" % "junit" % "4.11" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "ch.qos.logback" % "logback-classic" % "1.0.7",
    "org.scala-lang" % "scala-reflect" % "scalaVersion.value",
    "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.outworkers" %% "phantom-dsl" % "2.11.2" exclude("org.slf4j" ,"log4j-over-slf4j"),
  "com.outworkers" %% "phantom-connectors" % "2.11.2" exclude("org.slf4j" ,"log4j-over-slf4j"),
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0"
  cross CrossVersion.full exclude("orgslf4j", "log4j-over-slf4j")))
//  "com.datastax.spark" % "spark-cassandra-connector-unshaded_2.10"
//    % "2.0.0-M3" % Provided)

val datastax = Seq("com.datastax.spark" %
  "spark-cassandra-connector-unshaded_2.11" % "2.0.3")

val datastax3 = Seq("com.datastax.spark"
        %% "spark-cassandra-connector" % "2.0.3")

val sequencespark = Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0" % Provided,
  "org.apache.spark" %% "spark-sql" % "2.1.0" % Provided,
  "org.apache.spark"  %% "spark-mllib" % "2.1.0" % Provided)


lazy val root = project.in(file("."))
    	    	  .dependsOn(common % "compile->compile", ml)
        	  .settings(commonsettings, aggregate in update := false)

lazy val common =  { Project("common", file("common")).settings(
  commonsettings,
  libraryDependencies ++= sequence_common ++ sequencespark ++ datastax3)
}

lazy val ml = {
Project("ml", file("ml"))
    .dependsOn(common)//, scalaServer)
    .settings(commonsettings)

}

mergeStrategy in assembly <<= (mergeStrategy in assembly) {
  old => {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("javax", "servlet", xs @ _*)    => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*)     => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*)  => MergeStrategy.last
  case PathList("org", "apache", xs @ _*)       => MergeStrategy.last
  case PathList("com", "google", xs @ _*)       => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

//assemblyMergeStrategy in assembly := {
//  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
//  case PathList("javax", "servlet", xs @ _*)    => MergeStrategy.last
//  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
//  case PathList("javax", "inject", xs @ _*)     => MergeStrategy.last
//  case PathList("org", "aopalliance", xs @ _*)  => MergeStrategy.last
//  case PathList("org", "apache", xs @ _*)       => MergeStrategy.last
//  case PathList("com", "google", xs @ _*)       => MergeStrategy.last
//  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
//  case "plugin.properties" => MergeStrategy.last
//  case "log4j.properties" => MergeStrategy.last
//  case x =>
//    val oldStrategy = (assemblyMergeStrategy in assembly).value
//    oldStrategy(x)
//}


lazy val scalaServer = {
Project("scalaServer", file("scalaServer"))
.settings(
libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.3.4",
"org.scala-lang" % "scala-parser-combinators" % "2.11.0-M4"))
.dependsOn(common)
}




