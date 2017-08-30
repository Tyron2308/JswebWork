package Parsing

import akka.actor.ActorRef

import scala.util.Try

/***   Created by tyron on 13/06/17.
  */

sealed abstract class TokenDefinitio1n()
         extends Product with Serializable {
  val FAILURE       = "failure"
  val name          = this.toString.subSequence(0, this.toString.indexOf("("))

}

case class EndRead()
case class Ressource()
case class Response(str: String)
case class LaunchWorker(seq: Array[(String, String)], value: Long)
case class JobDone(index: Long, list: List[(String, List[Option[String]])])

case class Start()
case class Crash()
case class Dump()
case class Done()
case class toStock(seq: List[(String, List[String])])

