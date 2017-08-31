package Abstract

import modelAlgo.hyper
import akkahelper.LogEnum
import akkahelper.LogEnum.LogEnum
import helper.utiles
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by tyron on 01/08/17.
  */

trait LogMetric[Model] extends utiles {
  val famille: LogEnum = LogEnum.default
  type T = Model

  protected def searchFor(long: Long, arr: List[Rating]): Boolean = (arr) match {
    case head :: tail =>
      if (head.product == long) true
      else searchFor(long, tail)
    case Nil => false
  }

  protected def randomNegative(allAds: Int, guess: Int, it: (Int, Array[Rating]),
                             negat: ArrayBuffer[Int], r: Random)
  : ArrayBuffer[Int] = {
    if (!searchFor(guess, it._2.toList) && negat.size < 5)
      negat += guess
    negat.size == 5 match {
      case true => negat
      case false => randomNegative(allAds, r.nextInt(allAds), it, negat, r)
    }
  }

  /*** permet de formater le label de depart comme la matrix de predicton afin de comparer si oui ou non le model est efficace***/
  protected def addLabeledValue(all: Array[(Long, Int)],
                                matrix: Vector[(Long, Array[(Long, Int)])]):
  Vector[(Long, Array[(Long, Double)])] = {

    val tmp = all.map {
      case (id, count) => (id, count)
    }
    val cov = matrix.map {
      case (ip, product) =>
        (ip, product.map {
          case (id, rate) =>
            if (rate > 0) (id, 1.toDouble) else (id, 0.toDouble)
        })
    }
    todebug("add Label Value end")
    cov
  }

  def createLabelPrediction(label: Vector[Any],
                            ratings: Broadcast[Vector[((Int, Int), Double)]])
  : Vector[((Long, Long), (Double, Double))] = {
    val tmp = label.asInstanceOf[Vector[(Long, Array[(Long, Double)])]]
    val pd = tmp.flatMap {
      case (id, products) =>
        products.map {
          case (product, count) =>
            ((id, product), count)
        }
    }
    ratings.value.map {
      case ((id, product), rating) =>
        pd.toMap.get((id.toInt, product.toInt)) match {
          case Some(rate) => ((id.toLong, product.toLong),(rate, rating.toDouble))
          case None => ((id.toLong, product.toLong),(0.toDouble, rating))
        }
      case _ => ((-1.toLong, -1.toLong), (-3.0, -3.0))
    }
  }

  /*** merge label et predicton enssemble pour pouvoir appliquer methode spark (metric test) ***/
  def computeElem(sorted: Vector[((Int, Int), Double)],
                    alllab: Vector[(Long, Array[(Long, Double)])])
                   (implicit spark: SparkSession)
    : (Vector[((Long, Long), (Double, Double))], RDD[(Double, Double)]) = {

    @inline val x = (elem: ((Long, Long), (Double, Double))) => {
      elem match {
        case (id, (label, prediction)) =>
          if (label == prediction && label == -3.0) false else true
      }
    }
        val rsorted = spark.sparkContext.broadcast(sorted)
        val labelandprediction =
          createLabelPrediction(alllab.asInstanceOf[Vector[Any]], rsorted)


        val filtertest = labelandprediction.filter(x)
        val filtreid = filtertest.map { elem => elem._2 }

        val rdd = spark.sparkContext.parallelize(filtreid)
        ((filtertest), rdd)
  }


  /*** permet de creer deux set de test prediction "positive" et prediction "negative" ***/
  def buildFraction(toFraction: Vector[(Long, Array[(Long, Int)])],
                    allAds: Broadcast[Array[(Long, Int)]],
                    elem: Model,
                    modelcallback: (Model, Int, Int) => Array[Any],
                    spark: SparkSession)
  : (Vector[Any], Vector[Any])




/** dans l'idee d'etre generique et pouvoir adapter d'autre model par la suite **/
  @throws
  def transformationOnAny(data: Seq[Vector[Any]],
                          broad: Broadcast[Array[(Long, Int)]])
  : (Vector[((Int, Int), Double)], Vector[((Int, Int), Double)]) = {

    /*** reflechir a une meilleure maniere de gerer different modele ici ***/
    data.size >= 2 match {
      case true =>
        todebug("perform action on good and bad ")
        val goodratings = data(0).asInstanceOf[Vector[(Int, Array[Any])]].map {
          case (id, array) => (id, array.asInstanceOf[Array[Rating]])
        }

        val badratings  = data(1).asInstanceOf[Vector[(Int, Vector[Int])]]
       // val label       = data(2).asInstanceOf[Array[(Long, Array[(Long, Int)])]].toVector

        val gsorted = goodratings.flatMap {
            case (id, ratings) => ratings.map { case (Rating(id, product, rate) ) =>
            ((id, product), rate) }}
        val bad = badratings.flatMap { case (id, arr) => arr.map {
          elem => ((id, elem), 1.0) }}

        (gsorted, bad)
      case false =>
        throw new RuntimeException("error")
    }
  }

  def myMetricFunction(dataset: Seq[Vector[Any]],
                       broads: Broadcast[Array[(Long, Int)]],

                       modelname: String,
                       hyperparam: hyper,
                       spark: SparkSession)
  : Unit = {
    todebug("metric function dans overfit")
    try {
      val label = dataset(2)
        .asInstanceOf[Vector[(Long, Array[(Long, Int)])]]
      val (gsorted, bad) = transformationOnAny(dataset, broads)
      val alllabel = addLabeledValue(broads.value, label)
      val (solution, posList) = computeElem(gsorted, alllabel)(spark)
      val (negsolution, negative) = computeElem(bad, alllabel)(spark)
      toOverride(solution, posList, negsolution, negative)(hyperparam, modelname)
    }
    catch { case e: RuntimeException => e.printStackTrace }
  }

  /*** C'est dans cette fonction que la logic du metric doit etre faite.
  les parametres de cette fonction sont donnes par builfraction
    ***/
  def toOverride(positiveid:    Vector[((Long, Long), (Double, Double))],
                 poslabelpred:  RDD[(Double, Double)],
                 negativeid:    Vector[((Long, Long), (Double, Double))],
                 neglabelpred:  RDD[(Double, Double)])(hyper: hyper, modelname: String)
  : Unit
}
