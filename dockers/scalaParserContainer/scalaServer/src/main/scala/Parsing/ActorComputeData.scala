package Parsing

import java.util.Random

import akka.actor.Actor

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Created by tyron on 15/06/17.
  */

object ProcessInput {

  private val reqType       = """type=(.*?)(?=&)""".r
  private val boutique      = """boutique=(.*?)(?=[&HTTP])""".r
  private val amount        = """transaction_amount=(.*?)(?=[&HTTP])""".r
  private val idcategorie   = """categorie_id=(.*?)(?=[&HTTP])""".r
  private val produiid      = """produit_id=(.*?)(?=[&HTTP])""".r
  private val data          = """data=(.*?)(?=[&HTTP])""".r
  private val idtranscation = """transaction_id=(.*?)(?=[&HTTP])""".r

  private val pname         = """name=(.*?)(?=&)""".r
  private val pcategorie    = """category=(.*?)(?=&)""".r
  private val pproduct      = """product=(.*?)(?=&)""".r

  /*TODO = manque request*/

  def applyAll(string: String): List[Option[String]] = {

    List(reqType findFirstIn string,
      boutique findFirstIn string,
      amount findFirstIn string,
      idcategorie findFirstIn string,
      idtranscation findFirstIn string,
      pname findFirstIn string,
      pcategorie findFirstIn string,
      pproduct findFirstIn string,
      produiid findFirstIn string,
      data findFirstIn string)
  }

  def get(string: (String, String)): Future[(String, List[Option[String]])] = {
    Future {
      (string._1, applyAll(string._2))
    }
  }
}

object ActorComputeData {

  val detailBatch: Int = 50
}

class ActorComputeData(index: Int, token: Seq[(String, String)]) extends Actor {

  private var datastore : ListBuffer[(String, List[Option[String]])] = ListBuffer()
  val  key: Int = index

  token.foreach(elem =>  ProcessInput.get(elem) onComplete {
    case   Success(message) => {
      self ! message
    }
    case   Failure(ex) => println("FAILUREEEEEEEEE")
  })

  def receive: Receive = {
    case message: (String, List[Option[String]]) => {

      datastore += message
      if (datastore.size >= token.size) {

        context.parent ! JobDone(key, datastore.toList)
        datastore = datastore.drop(ActorComputeData.detailBatch)
      }

    }
    case Done => context.stop(self)
  }

  def stop(body: List[Option[String]]): Unit = {
    context.stop(self)
  }

}
