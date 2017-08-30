
import akka_tcp_server._
import database._
import Parsing._
import com.outworkers.phantom.dsl._
import akka.actor._

object Maine {
  def main(args: Array[String]): Unit = {
    DmpDatabase.create()
    
    val system = ActorSystem("ServerSystem")
    val server = system.actorOf(Props[Server], "serverActor")
  }
}
