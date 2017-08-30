package tcp_server

import Parsing._
import database._
import com.datastax.driver.core.utils.UUIDs
import java.net.ServerSocket
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.PrintStream

class TCPServer(ip: String, port: Int) {
  val ss = new ServerSocket(port)
  val parser = LogParser
  val table = DmpDatabase.users

  def listen(): Unit = {
    val sock = ss.accept()
    val is = new BufferedInputStream(sock.getInputStream())
    val os = new PrintStream(new BufferedOutputStream(sock.getOutputStream()))

   /* def loop(is: BufferedInputStream): Unit = {
      while (is.available() < 1) { Thread.sleep(100) }
      val buf = new Array[Byte](is.available)
      is.read(buf)
      val in = parser.parse(new String(buf))
      table.store(new Record(UUIDs.timeBased(),
        in(0),
        in(1),
        in(2),
        in(3),
        in(4),
        in(5),
        in(6),
        in(7),
        in(8),
        in(9),
        in(10)))
      println(in)
      loop(is)
    }*/

    //loop(is)
  }
}

object TCPServer extends TCPServer("127.0.0.1", 5005)
