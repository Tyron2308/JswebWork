package akka_tcp_server

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import akka_tcp_server.ParsingHelper


class Server extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", 5005))

  def receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[ParsingHelper])
      val connection = sender ()
      connection ! Register(handler)
  }
}
