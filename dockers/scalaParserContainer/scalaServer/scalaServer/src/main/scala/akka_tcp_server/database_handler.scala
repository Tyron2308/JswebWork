package akka_tcp_server

import akka_tcp_server.ParsingHelper
import database._
import com.datastax.driver.core.utils.UUIDs
import akka.io.Tcp
import akka.actor.Actor
import scala.collection.mutable.ListBuffer

object DatabaseHandler {
  var list: ListBuffer[List[String]]  = ListBuffer()
  case class Dump()
  case class DataReceive(Array :Array[String])
}

class DatabaseHandler extends Actor {
  import Tcp._
  import Parsing._
  val parser                = LogParser
  val table                 = DmpDatabase.users

  def store(rdd: Array[String]): Unit = {
    val in = parser.parse(rdd)
    in isDefined match {
      case true => {
        val tomap = in.get
        tomap.map(elem => DatabaseHandler.list += elem._2)
        val tmp =  tomap.map(list => new Record(UUIDs.timeBased(),
          list._2(0),
          list._2(1),
          list._2(2),
          list._2(3),
          list._2(4),
          list._2(5),
          list._2(6),
          list._2(7),
          list._2(8),
          list._2(9),
          list._2(10),
          list._2(11),
          list._2(12),
          list._2(13),
          list._2(14),
          list._2(15),
          list._2(16),
          list._2(17),
          list._2(18)))
        tmp.map(record => table.store(record))
      }
      case false => println("FAILURE !!!!!! ")
    }
  }

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  override def receive: Receive = {

    case DatabaseHandler.DataReceive(rdd) => {
      store(rdd)
    }

    case DatabaseHandler.Dump => {
    sender ! ParsingHelper.Dumpbacth(DatabaseHandler.list)
    }

    case PeerClosed     => context stop self
  }
}
