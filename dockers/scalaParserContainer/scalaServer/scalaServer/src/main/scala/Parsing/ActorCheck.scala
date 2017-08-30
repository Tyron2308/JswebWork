package Parsing

import java.util.NoSuchElementException

import Parsing.ActorCheck.{CheckL, CheckList}
import akka.actor.Actor

/**
  * Created by tyron on 15/06/17.
  */

object ActorCheck {
  case class CheckList(seq: List[(String, List[String])])
  case class CheckL(seq: List[(String, List[Option[String]])])
}

class ActorCheck() extends Actor {

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  def receive: Receive = {

    case Start=> {
      context.parent ! Done
    }
    case CheckL(datastore) => {
      val rd = datastore
        .map(elem => (elem._1 ,elem._2.map(el => if (el.isDefined) el.get else "None")))
        sender ! CheckList(rd)
        self ! Start
    }
  }
}
