package akka_tcp_server

import akka.actor.{Actor}
import akka.io.Tcp

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