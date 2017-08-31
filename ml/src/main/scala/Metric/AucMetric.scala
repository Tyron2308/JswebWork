package Metric

import Abstract.LogMetric
import modelAlgo.{Hyperparameter, hyper}
import akkahelper.LogEnum
import akkahelper.LogEnum.LogEnum
import com.datastax.driver.core.utils.UUIDs
import database._
import databasehelper.AucLog
import helper.utiles
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
/**
  * Created by tyron on 24/07/17.
  */

class AucMetric(metricDatabase: AucDatabase.users.type,
                spark: SparkSession)
  extends java.io.Serializable with utiles
    with LogMetric[MatrixFactorizationModel] {

  override val famille: LogEnum = LogEnum.aucMetric


  /*** performe roc sur label and prediciton ***/
  def receiverOperatingCharacter(labelandprediction: RDD[(Double, Double)])
  : (Long, Long, Long, Long) = {

    @inline val x = (label: (Double, Double), callback: Boolean) => {
      label match {
        case (label, prediction) if (callback) == true => true
        case _ => false
      }
    }
    val truepos = labelandprediction.filter{
      elem => x(elem, {elem == elem._2})
    }.count()
    val falsepositive = labelandprediction.filter {
      elem => x(elem, {elem._2 > 0 && elem._1 == 0})
    }.count()
    val falsenegative = labelandprediction.filter {
      elem => x(elem, {elem._2 == 0 && elem._1 > 0})
    }.count()
    val truenega = labelandprediction.filter {
      elem => x(elem, {elem._2 == 0 && elem._1 == 0})
    }.count()
    (truepos, falsepositive, falsenegative, truenega)
  }

  /*** exemple de logique de metric, store sur une database ensuite pour pouvoir les resultats ***/
  override def toOverride(positiveid:   Vector[((Long, Long), (Double, Double))],
                          poslabelpred: RDD[(Double, Double)],
                          negativeid:   Vector[((Long, Long), (Double, Double))],
                          neglabelpred: RDD[(Double, Double)])
                         (hyper: hyper , modelname: String): Unit = {

    val (ntp, nfp, nfn, ntn) = receiverOperatingCharacter(neglabelpred)
    val (tp, fp, fn, tn)     = receiverOperatingCharacter(poslabelpred)
    val negmetric            = new BinaryClassificationMetrics(neglabelpred)
    val posmetric            = new BinaryClassificationMetrics(poslabelpred)
    val posauc_ref           = posmetric.areaUnderROC()
    val negauc_ref           = posmetric.areaUnderROC()
    val count                = poslabelpred.count()
    val m                    = hyper.asInstanceOf[Hyperparameter]
    val list : List[Double] = List(m.iterationn(0), m.alpha, m.rank, m.reg)

    metricDatabase.store(new AucLog(UUIDs.timeBased(),
                         modelname, 0, list,
      positiveid.toList, List[Double](tp.toDouble, fp.toDouble,
        fn.toDouble, tn.toDouble, posauc_ref.toDouble, count.toDouble)))

    metricDatabase.store(new AucLog(UUIDs.timeBased(), modelname, 1,
      list, negativeid.toList, List[Double](ntp.toDouble,
        nfp.toDouble, nfn.toDouble, ntn.toDouble,
        negauc_ref.toDouble, count.toDouble)))

    todebug("metricdatabase store !!!! ")
  }


  /*** Dans l'etat actuel des choses il est obligatoire d'override cette fonction a chaque fois. desole haha***/
  override def buildFraction(toFraction: Vector[(Long, Array[(Long, Int)])],
                             allAds: Broadcast[Array[(Long, Int)]],
                             elem: MatrixFactorizationModel,
                             modelcallback: (MatrixFactorizationModel, Int, Int) => Array[Any],
                             spark: SparkSession)
  : (Vector[Any], Vector[Any])
  = {

    todebug("build fraction dans AucLog")
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
          randomNegative(allAds.value.length, guess.toInt, (id, transform),
            negativeSet, random)
          negativeSet.toIterator
      })
    }

    (withfilter.asInstanceOf[Vector[Any]],
      negpoint.asInstanceOf[Vector[Any]])
  }
}