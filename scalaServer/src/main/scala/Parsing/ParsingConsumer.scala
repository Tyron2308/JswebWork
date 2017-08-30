package Parsing

import Parsing.ActorCheck.{CheckL, CheckList}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}

import scala.collection.mutable.ListBuffer

/**
  * Created by tyron on 15/06/17.
  */

class ParsingConsumerActor extends Actor {

  private var save      : ListBuffer[(String, List[String])]  = ListBuffer()
  private var workers   : ListBuffer[Option[ActorRef]]                = ListBuffer()
  private var checker   : Option[ActorRef]                            = None
  private var mresponse : Option[ActorRef]                            = None
  private var mcount    : Int                                         = 0
  private var flg       : Int                                         = 0

  override val supervisorStrategy: SupervisorStrategy = {
    val decider : SupervisorStrategy.Decider  = {
      case ParsingProducerActor.CrashException => SupervisorStrategy.Restart
    }
    OneForOneStrategy()(decider orElse super.supervisorStrategy.decider)
  }

  private def emptyBatch(batch: Array[((String, String), Int)], msize: Long, count :Int): Boolean = {

    if (count <= msize) {
      val tmp = batch.filter(e => e._2 <= count)
      val mytho = tmp.map(el => el._1)
      workers += Some(context.actorOf(Props[ActorComputeData]
        (new ActorComputeData(0, tmp.map(_._1)))))
      val newBatch = batch.filter(elem => elem._2 > count)
      return emptyBatch(newBatch, msize,
        workers.size * utils.batchdecompose)
    }
    true
  }

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  override def receive: Receive = {

    case LaunchWorker(batch, number) => {
      if (flg == 0 ) {
        mresponse = Some(sender)
        flg += 1;
      }
      val b = batch.zipWithIndex
      emptyBatch(b, number, utils.batchdecompose)
      self ! Done
    }

    case JobDone(index, seq) => {
      checker = Some(context.actorOf(Props[ActorCheck] (new ActorCheck)))
      if (checker.isDefined) checker.get ! CheckL(seq)
    }

    case CheckList(data) => {
      data.map(save += _)
    }

    case EndRead =>  {
      mresponse.get ! Done
      mresponse.get ! Ressource
    }

    case Done =>
      if (mcount == (workers.size -1)) {
        mresponse.get ! toStock(save.toList)
        self ! EndRead
      }
      mcount += 1
  }
}

object ParsingConsumerActor {
  def make(system: ActorSystem): ActorRef = {
    system.actorOf(Props[ParsingConsumerActor], "consumer")
  }
}
