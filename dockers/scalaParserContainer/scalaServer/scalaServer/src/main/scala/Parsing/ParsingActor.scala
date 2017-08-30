package Parsing

/**
  * Created by tyron on 14/06/17.
  */

import java.util.concurrent.TimeUnit

import akka.actor.{Actor,ActorRef, ActorSystem, Props}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object ParsingActorManager {

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  def initActor(rdd: Array[(String, List[String])]): Option[List[(String, List[String])]] = {
    try {
      val t0 = System.nanoTime()
      val sys = ActorSystem("ParsingProducer")
      val pC = sys.actorOf(Props[ParsingConsumerActor], "consumer")
      val pP = sys.actorOf(Props[ParsingProducerActor]
        (new ParsingProducerActor(pC, rdd)))
      val timeout = Timeout(5000 second)
      val future = pP.ask(Ressource)(timeout)

      val result = Await.result(future, timeout.duration)
                        .asInstanceOf[List[(String, List[String])]]

      val duration = (System.nanoTime - t0) / 1e9d
      println("duration ===>>>>> " + duration)
      val res = Some(result)
      val t1 = System.currentTimeMillis()
      res
    }
    catch {
      case e:OutOfMemoryError => e.printStackTrace.toString ; None
  }
  }
}

class ParsingProducerActor(cosum: ActorRef, rdd: Array[(String, List[String])]) extends Actor{

  private var mconsumer : Option[ActorRef]                      = Some(cosum)
  private var mresponse : Option[ActorRef]                      = None
  private var bool      : Boolean                               = false
  private var datastore : List[(String, List[String])]          = List()
  private var flg       :  Int                                  = 0

  if (mconsumer.isDefined)
    self ! Start

  println(" depart " + System.currentTimeMillis())
  override def receive: Receive = {

    case Ressource =>{
      if (flg == 0) {
        mresponse = Some(sender)
        flg += 1
      }
      if (bool == true) { mresponse.get ! datastore }
    };
    case Start => {
      val batch = extractBatch(rdd)

      mconsumer.get ! LaunchWorker(batch, ParsingProducerActor.batchSize)
    }
    case toStock(data) => {
      datastore = data
    }
    case Done => bool = true ;
  }

  def extractBatch(rdd: Array[(String, List[String])]): Array[(String, String)] = {
    val tmp = rdd.map(elem =>
      (elem._1, elem._2(ParsingProducerActor.elemtoParse)))
    tmp
  }
}

object ParsingProducerActor {

  private val elemtoParse: Int  = 4
  /* cette variable determine la taille du bach a parser*/
  private val batchSize: Int = 100

  case object CrashException extends RuntimeException
  def make(system: ActorSystem): ActorRef = {
    system.actorOf(Props[ParsingProducerActor], "producer")
  }
}

