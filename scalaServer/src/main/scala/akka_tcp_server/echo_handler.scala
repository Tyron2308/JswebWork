package akka_tcp_server

import akka.actor.{Actor}
import akka.io.Tcp
import scala.collection.mutable.ListBuffer

object EchoHandler {
  val batchSize = 100
  /** sert a afficher une dataframe a la fin si besoin */
  val debug = 1
  case class Dumpbacth(listBuffer: ListBuffer[List[String]])
}

class EchoHandler extends Actor {
  import Tcp._

  def receive = {
    case Received(data) => {
      val string = data.decodeString("US-ASCII")
      println(string)
    }

    case PeerClosed => context stop self
  }
}
