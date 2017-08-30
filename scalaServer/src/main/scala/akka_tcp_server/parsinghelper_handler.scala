package akka_tcp_server

/**
  * Created by tyron on 10/07/17.
  */


package akka_tcp_server

import akka.actor.{Actor, Props}
import akka.io.Tcp
import scala.collection.mutable.ListBuffer

object ParsingHelper {
  val batchSize = 100
  /** sert a afficher une dataframe a la fin si besoin */
  val debug = 1
  case class Dumpbacth(listBuffer: ListBuffer[List[String]])
}

class ParsingHelper extends Actor {
  import Tcp._

  /** index de debug, supprimer par la suite **/
  var index = 0
  var datastore: ListBuffer[String] = ListBuffer()

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  todebug("Va commencer a recevoir des logs")
  def receive = {
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
      if (string.equals("Done")) {
        todebug("a recue un done ficher fini d'etre lue")

        ParsingHelper.debug == 1 match {
          case true => self ! PeerClosed
          case false => {
            if (datastore.size > 0) {
              println("datastore size " + datastore.size)
              todebug("data store size > 0")
            }
            todebug("send dump to actorindex")
            val actorRe = context.actorOf(Props[DatabaseHandler])
            actorRe ! DatabaseHandler.Dump
          }
        }
      }
      if (datastore.size >= ParsingHelper.batchSize) {
        index += datastore.size
        todebug("NEW BATCH" + index)
        val actorRe = context.actorOf(Props[DatabaseHandler])
        actorRe ! DatabaseHandler.DataReceive(datastore.toArray)
        datastore = datastore.drop(ParsingHelper.batchSize)
      }
    }

    case PeerClosed => context stop self
  }
  /** Ce cas sert a creer une dataframe **/
  /*  case EchoHandler.Dumpbacth(batch) => {

      val schema  = List("ip", "date", "time","produit_id",
        "replyCode", "device", "debug",
        "reques", "boutique", "amount", "idcat", "produitid",
        "data", "idtransaction", "pname", "pcategory",
        "pproduct")

      def createSchema(list: List[String]): StructType = {
        StructType(list.map(arg => StructField(arg, StringType, true)))
      }

      def row(list: List[String]): Row = {
        Row.fromSeq(list)
      }

      val structure = createSchema(schema.flatMap(elem => elem.split(" ").to[List]))
      val rdd = ss.sparkContext.parallelize(batch)
      val tmp = rdd.map(p => List(p(0), p(1), p(2), p(4), p(6),
                              p(7), p(8), p(9), p(10), p(11),
                              p(12), p(13), p(14), p(15), p(16),
                              p(17), p(18)))

      val dataframe = ss.createDataFrame(tmp.map(p => row(p)), structure)
      dataframe.show(50)
      ss.stop()
    }
  }*/
}
