package Preprocessing
/**
  * Created by tyron on 08/07/17.
  */

import akka.util.Timeout
import com.outworkers.phantom.dsl.ListResult
import database._
import helper.utiles
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

import scala.concurrent.Await


/*** creer la matrix des data points, transforme les string boutique et les ip en ID ***/
class RetrieveInformation(spark : SparkSession)
  extends java.io.Serializable with utiles {

  /*** mettre sous form try catch et creer une fonction
  dans utile genertic a toute les databse ***/

  import spark.implicits._
  todebug("print les boutique de ma list")
//  try {
    val tablestore = DmpDatabase.users
    val timeout = new Timeout(500000)
    val list = tablestore.myselect()
    val result = Await.result(list, timeout.duration)
      .asInstanceOf[ListResult[Record]]
    val todf = result.records
      .map { elem => (elem.ip, elem.shop) }
      .toDF("ip", "boutique")

    todf.show(10)

  def mapToIndex(mapString: List[(String, Array[(String, Int)])],
                 IpwithIndex: Map[String, Long], boutiquewithIndex: Map[String, Long])
  : Map[Long, Array[(Long, Int)]] = {

    def addToMap(current: (String, Array[(String, Int)])): (Long, Array[(Long, Int)]) = {
       IpwithIndex.get(current._1) match {
         case Some(x : Long) => {
           val curr  = current._2.map { elem =>
             boutiquewithIndex.get(elem._1) match {
               case Some(vlue: Long) =>
                 (vlue -> elem._2)
             }
           }
           (x -> curr)
         }
       }
    }
    mapString.map { elem => addToMap(elem)}.toMap
  }

  def count_productUser(te: Map[String, Array[(String, Int)]]): Map[String, Array[(String, Int)]] = {
    te.mapValues { elem =>
      if (elem.length > 1) {
        elem.foldLeft(Map[String, Int]())((accum, curr) => {
          val map = elem.toMap
          map.get(curr._1) match {
            case Some(value: Int) =>
              map + (curr._1 -> (value + curr._2))
            case None =>
              accum + (curr._1 -> curr._2)
          }}).toArray
      }
      else elem
    }
  }

  def process_dataToCollect(withIndex: Map[String, Long], IpwithIndex: Map[String, Long])
  : (Map[String, Array[(String, Int)]],
    Option[Map[Long, Array[(Long, Int)]]]) = {

    def assertValid(map: Map[String, Array[(String, Int)]]):
    Option[Map[Long, Array[(Long, Int)]]] = {
      val newmap = mapToIndex(map.toList, withIndex, IpwithIndex)
      newmap.size == map.size match {
        case true => Some(newmap)
        case true => None
      }
    }


    val regroup = todf.map {
      elem => (elem.getAs[String](0),
        elem.getAs[String](1)) }.collect().groupBy(_._1)

    val countPair = regroup.mapValues {
      k => (k.map(elem => (elem._2, 1)))
    }

    val mapWstring = count_productUser(countPair)
    val end = assertValid(mapWstring)
    end match {
      case Some(map) =>
        todebug("YESSSS")
        (mapWstring, end)
      case None =>
        todebug("NOOOOO")
        (mapWstring, None)
    }
  }

  private val ip               = todf.map { elem => (elem.getAs[String](0))}.distinct.rdd.cache
  private val boutique         = todf.map { elem => (elem.getAs[String](1))}.distinct.rdd.cache
  val indexIp                  = ip.zipWithUniqueId()
                                   .collect().toMap
  val indexBoutique            = boutique.zipWithUniqueId()
                                         .collect().toMap
  val bindexIp                 = spark.sparkContext
                                      .broadcast(indexIp.map{ k => k.swap })


  val bboutique                = spark.sparkContext
                                      .broadcast(indexBoutique.map{ k => (k._2, 0)}.toArray)
  val(matrixString, matrixInt) = process_dataToCollect(indexIp, indexBoutique)

  ip.unpersist()
  boutique.unpersist()

}