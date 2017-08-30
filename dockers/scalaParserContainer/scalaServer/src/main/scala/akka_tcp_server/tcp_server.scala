package akka_tcp_server

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress

import akka_tcp_server.ParsingHelper

import scala.collection.mutable.ListBuffer


class Server extends Actor {
  import Tcp._
  import context.system

  val batch = 10
  var index = 0
  var datastore: ListBuffer[String] = ListBuffer()

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", 5005))

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  def receive = {
    case b @ Bound(localAddress) =>
      context.parent ! b

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val connection = sender ()
      connection ! Register(self)

    case Received(data) => {
      val string = data.decodeString("US-ASCII")
      if (!string.contains("GET /su")
        || !string.contains("GET /boot/")
        || !string.contains("GET /banner/")
        || !string.contains("GET /ado/")
        || !string.contains("GET /muri/")
        || !string.contains("GET /ee/")
        || !string.contains("GET /ee/aa")
        || !string.contains("GET /ee/ii")
        || !string.contains("Done")
        || !string.contains("GET /mui/")) {
        datastore += string
      }
      if (datastore.size >= batch) {
        index += datastore.size
        todebug("NEW BATCH" + index)
        val actorRe = context.actorOf(Props[DatabaseHandler])
        actorRe ! DatabaseHandler.DataReceive(datastore.toArray)
        datastore = datastore.drop(batch)
      }
    }
  }
}
