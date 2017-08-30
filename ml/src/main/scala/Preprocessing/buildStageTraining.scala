import akka.util.Timeout
import com.outworkers.phantom.dsl.ListResult
import database.{Record}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.concurrent.Await

/**
  * Created by tyron on 24/07/17.
  */

/*
class buildStageTraining(spark : SparkSession)
  extends java.io.Serializable with utiles {
    import spark.implicits._

  def buildCount(dataset: RDD[(Long, Long)]):
  RDD[(Long, Array[(Long, Int)])] = {

    val update = dataset.aggregate(Seq[(Long, Array[(Long, Int)])]())((acc, curr) => {
        val map = acc.toMap
        map.get(curr._1) match {
          case Some(array) =>
            val pd = array ++ Array((curr._2, 1))
            val d =   (curr._1, pd)
            val tmp = Seq(d)
            tmp ++ acc
          case None =>
            acc :+ (curr._1, Array((curr._2, 1)))
        }
    },
    (elem, curr) => {
      curr.map {
        case (ip, product) =>
          (ip, product.foldLeft(Array[(Long, Int)]())((accum, curr) => {
            val map = accum.toMap
            map.get(curr._1) match {
              case Some(array) =>
                accum.update(accum.indexOf(curr, 0),
                  (curr._1, array + curr._2))
                accum
              case None =>
                accum :+ (curr)
            }}))
      }
    })

    spark.sparkContext.parallelize(update)
  }

  def getDataframe(): sql.DataFrame = {
    todebug("print les boutique de ma list")
    val tablestore = DmpDatabase.users
    val timeout = new Timeout(500000)
    val list = tablestore.myselect()
    val result = Await.result(list, timeout.duration)
      .asInstanceOf[ListResult[Record]]
    result.records
      .map { elem => (elem.ip, elem.boutique) }
      .toDF("ip", "boutique")
  }

  def buildAlias(): (sql.DataFrame, RDD[(Long, Long)]) = {
    import spark.implicits._
    val test      = dataframe.rdd
    val tmp       = test.map { elem =>(elem.getAs[String](0), elem.getAs[String](1)) }
    val joinded   = tmp.join(indexIp).map {
      case (ip, (boutique, alis)) => (boutique, (ip, alis))
    }

    val tm        = joinded.join(indexbtik)
    val rdd       = tm.map {
      case (boutique, ((ip, aliasip), aliasbtique)) =>
        (aliasip, aliasbtique)
    }
    (tm.toDF(), rdd)
  }

  todebug("FDP1")
  val dataframe           = getDataframe()


  todebug("FDP2")
  private val indexIp     = dataframe.select("ip")
                             .as[String].rdd.distinct()
                             .zipWithUniqueId()
                             .mapValues { case (log) => log }
  todebug("FDP3")
  val tmp                 = dataframe.select("boutique")
                                     .as[String].rdd.distinct()
                                     .zipWithUniqueId()

  todebug("FDP4")
  val boutique            = tmp.map{ case (str, long) => long }
  private val indexbtik   = tmp.mapValues{ case (log) => log }


  todebug("FDP5")
  val (all, rdd)          = buildAlias()
*/


  //val withcount           = buildCount(rdd)


 //withcount.toDF().show()


/*  todebug("dataframe")
  dataframe.show()
  todebug("all")
  all.show()
  todebug("withcount")


  todebug("indexIp")
  indexIp.toDF().show()

  todebug("indexbtik")
  indexbtik.toDF().show()*/
//}
