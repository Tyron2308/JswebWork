package Metric

import Abstract.LogMetric
import modelAlgo.{Hyperparameter, hyper}
import akkahelper.LogEnum
import akkahelper.LogEnum.LogEnum
import com.datastax.driver.core.utils.UUIDs
import database.MetricDatabase
import helper.utiles
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by tyron on 13/07/17.
  */
import databasehelper._

class RmseMetric(metricDatabase: MetricDatabase.users.type, spark: SparkSession)
  extends LogMetric[MatrixFactorizationModel] with utiles {

  override val famille: LogEnum = LogEnum.aucMetric

  def toOverride(positiveid:    Vector[((Long, Long), (Double, Double))],
                 poslabelpred:  RDD[(Double, Double)],
                 negativeid:    Vector[((Long, Long), (Double, Double))],
                 neglabelpred:  RDD[(Double, Double)])
                (hyper: hyper, modelname: String): Unit = {

    val negative_regression     = new RegressionMetrics(poslabelpred)
    val positive_regression     = new RegressionMetrics(neglabelpred)
    val positivereg             = positive_regression.rootMeanSquaredError
    val negativereg             = negative_regression.rootMeanSquaredError

    todebug("AUCCCCCCCCCCCCCCCCCC METRIC RMSE ")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC RMSE")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC RMSE ")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC RMSE")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC RMSE ")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC RMSE")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")
    todebug("AUCCCCCCCCCCCCCCCCCC METRIC")

    val m = hyper.asInstanceOf[Hyperparameter]

    val list : List[Double] = List(m.iterationn(0).toDouble, m.alpha, m.rank.toDouble, m.reg)

    metricDatabase.store(new RmseLog(UUIDs.timeBased(), modelname, 0,
                        list,positiveid.toList, positivereg))

    metricDatabase.store(new RmseLog(UUIDs.timeBased(), modelname, 1,
                         list, negativeid.toList, negativereg))
    todebug("end writting")
  }

  override def buildFraction(toFraction: Vector[(Long, Array[(Long, Int)])],
                             allAds: Broadcast[Array[(Long, Int)]],
                             elem: MatrixFactorizationModel,
                             modelcallback: (MatrixFactorizationModel, Int, Int) => Array[Any],
                             spark: SparkSession)
  : (Vector[Any], Vector[Any])
  = {

    todebug("build fraction dans RMSE metric")
    val pospoint = toFraction.map {
      case (ip, product) =>
        try {
          (ip.toInt, modelcallback(elem, ip.toInt, 5))
        }
        catch {
          case e: NoSuchElementException =>
            (-1, Array.empty[Any])
        }
    }


    import spark.implicits._
//    spark.sparkContext.parallelize(pospoint).toDF().show()


    val withfilter = pospoint.filter {
      case (ip, array) if (ip != -1) => true
      case _ => false
    }
    val negativeSet = new ArrayBuffer[Int]()
    val negpoint = toFraction.map { case (ip, elem) =>
      val random = new Random()
      negativeSet.clear()
      (ip.toInt, withfilter.flatMap {
        case (id, pro) =>
          val transform = pro.asInstanceOf[Array[Rating]]
          val tm = allAds.value.map {
            _._1
          }
          val guess = tm(random.nextInt(allAds.value.length))
          randomNegative(allAds.value.length, guess.toInt, (id, transform), negativeSet, random)
          negativeSet.toIterator
      })
    }
    (withfilter.asInstanceOf[Vector[Any]], negpoint.asInstanceOf[Vector[Any]])
  }
}

