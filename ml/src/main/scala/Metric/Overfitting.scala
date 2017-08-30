package Metric
import Abstract.LogMetric
import modelAlgo.Model
import akka.util.Timeout
import akkahelper._
import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.dsl.ListResult
import database.{AucDatabase, OverfitDatabase, User, UserDatabase}
import databasehelper.{AucLog, OverfitLog}
import helper.utiles
import org.apache.hadoop.fs.PathNotFoundException
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import modelAlgo._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Random

final object path {
  final val pathmodel = ""
  final val MODELNUMBER = 10
}


object OverfitingMetric extends utiles {

  def toRun(): Unit = {
  /*** define function to run for overfit metric ***/
  }
}

class OverfitingMetric(metricDatabase: OverfitDatabase.users.type,
                        spark : SparkSession) extends Model(spark)
  with LogMetric[MatrixFactorizationModel] {

  def asynchRead(): Future[ListResult[AucLog]] = {
    todebug("print les boutique de ma list")
    val tablestore = AucDatabase.users
    tablestore.myselect()
  }

  def asynchRead(bool: Boolean): Future[ListResult[OverfitLog]] = {
    metricDatabase.myselect()
  }

  todebug("<-- To READ ---> ")
  val listAucLog = asynchRead()
  val timeout = new Timeout(500000)
  val result = Await.result(listAucLog, timeout.duration)
    .asInstanceOf[ListResult[AucLog]]

  todebug("apply changement ---> ")
  val todf = result.records.map { elem =>
    ((elem.modelname, elem.good),
      elem.roclist(3))
  }

  todebug("giga importabt")
  result.records.map(elem =>
    print("MEGA IMPORTANT " + elem.labelpred.length))
  todebug("giga importabt2")


  val rdd = spark.sparkContext.parallelize(todf).cache()
  rdd.count()


  import spark.implicits._

  rdd.toDF().show()

  /** qui va selectionner les modeles pour lui envoyer sous cette forme ? **/
  override def runMetricOnModel(mapModel:
                                Map[String, (hyper, MatrixFactorizationModel)],
                                evalmaster: EvaluaterHelpere[MatrixFactorizationModel,
                                  (Long, Array[(Long, Int)])])(implicit mu: LogMetric[MatrixFactorizationModel], seq: Seq[Any])
  : Unit = {

    todebug("run Metric On Model dans overfiting")
    for (elem <- mapModel) {
      val reecu = elem._2._1
      evalmaster.asInstanceOf[EvaluateHelper]
        .applyMetrics(seq, elem._1,
          reecu.asInstanceOf[Hyperparameter])(this)
    }
    todebug("done apply metric overfiting")
  }

  def scalerating(r: Rating): Rating = {
    val scaledRating = math.max(math.min(r.rating, 1.0), 0.0)
    Rating(r.user, r.product, scaledRating)
  }

  override def trainData(matrix: RDD[(Long, Array[(Long, Int)])])
  : RDD[Any] = {
    todebug("TRAIN DATA ")
    todebug("traindata dans overfiting")

    import spark.implicits._
    matrix.toDF().show(10)

    val scale = matrix.map {
      case ((ip, product)) =>
        (ip.toLong,
          product.map { case (product, count) =>
            val scaledRating = math.max(math.min(count, 1), 0)
            (product.toLong, scaledRating)
          })
    }
    todebug("end")
    //RDD[(Long, Array[(Long, Int)])]
    scale.asInstanceOf[RDD[Any]]
  }

  def toOverride(positiveid: Vector[((Long, Long), (Double, Double))],
                 poslabelpred: RDD[(Double, Double)],
                 negativeid: Vector[((Long, Long), (Double, Double))],
                 neglabelpred: RDD[(Double, Double)])
                (hyper: hyper, modelname: String): Unit = {

    val selected = rdd.filter { case ((name, good), list) =>
      name.contains(modelname) && good == 0
    }


    val coutn = selected.count()
    val withmean = selected.reduceByKey((k, v) => {
      (k / v)
    }).mapValues { case (rate) => (rate / coutn) }

    todebug("perform classification metric with nwe trainig")
    val posmetric = new BinaryClassificationMetrics(poslabelpred)
    val posauc_ref = posmetric.areaUnderROC()
    val samemodel = withmean.lookup((modelname, 0))

    //COMPte le nombre de fois ou mon model a un meilleur
    // auc avec le cvset que le trianing set
    val bestscore = samemodel.count { case (mean) =>
      if (mean > posauc_ref) true else false
    }

    val m = hyper.asInstanceOf[Hyperparameter]
    val list: List[Double] = List(m.iterationn(0), m.alpha, m.rank, m.reg)

    val bool = (bestscore > 0) match {
      case true => 0
      case false => 1
    }

    val overfit = new OverfitLog(UUIDs.timeBased(),
      modelname, bool, list,
      positiveid.toList, bestscore, posauc_ref)

    metricDatabase.store(overfit)
    /** * save model qui ont passer le test, save parameter, ecrire product feature ***/
  }

  override def reloadmyModel(index: Int, path: String)
  : Option[MatrixFactorizationModel] = {
    try {
      Some(MatrixFactorizationModel.load(spark.sparkContext, path))
    }
    catch {
      case e: PathNotFoundException =>
        e.printStackTrace
        None
      case t: RuntimeException =>
        t.printStackTrace
        None
    }
  }

  override def buildFraction(toFraction: Vector[(Long, Array[(Long, Int)])],
                             allAds: Broadcast[Array[(Long, Int)]],
                             elem: MatrixFactorizationModel,
                             modelcallback: (MatrixFactorizationModel, Int, Int) => Array[Any],
                             spark: SparkSession)
  : (Vector[Any], Vector[Any]) = {

    todebug("<----- buildFraction MA FRERE -----> ")
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
          randomNegative(allAds.value.length, guess.toInt,
            (id, transform), negativeSet, random)
          negativeSet.toIterator
      })
    }
    (withfilter.asInstanceOf[Vector[Any]], negpoint.asInstanceOf[Vector[Any]])
  }

  rdd.unpersist()

  def loadValidedMode(boutique: Map[String, Long],
                      ip: Map[String, Long],
                      databaseuser: UserDatabase.users.type): Unit = {
    todebug("LOAD VALID MODEL ----> ")
    /** burk a modifier au plus vite mais pas important pour le moment ***/

    val load = asynchRead(false)
    todebug("load read")
    val resultat = Await.result(load, timeout.duration)
      .asInstanceOf[ListResult[OverfitLog]]
    val todf = resultat.records.map { elem => (elem.ancien, elem.reel, elem.labelpred) }
    val rdd = spark.sparkContext.parallelize(todf).cache()

    val listpred = result.records.map { case elem =>
      (elem.labelpred, elem.modelname)
    }

    rdd.count()
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")
    todebug("VALID MODEL ")

    todebug("probleme")
    val user = listpred.flatMap {
      elem =>
        elem._1.map {
          ele => (elem._2, ele._1)
        }
    }

    import spark.implicits._
    todebug("12222")
    val s = spark.sparkContext.parallelize(user)
    s.toDF().show()
    val flemme = s.groupBy(_._1).collect()


    todebug("icic.....................")
//    flemme.toDF().show()

    todebug("LAaaaaaaaaaaaaaa ste[ 1 ")
    val tmp = ip.map { elem => elem.swap }
    val tmpboutique = boutique.map { elem => elem.swap }
    todebug("LAaaaaaaaaaaaaaa ste[ 2 ")


    val transform = flemme.flatMap {
      elem =>
        elem._2.map {
          node =>
            tmp.get(node._2._1) match {
              case Some(string) =>
                tmpboutique.get(node._2._2) match {
                  case Some(str) => (string, str)
                  case None => (string, "fail")
                }
              case None => ("fail", "fail")
            }
        }
    }

        todebug("affiche transform ===>")
    import spark.implicits._
  //  transform.toDF().show()
        spark.sparkContext.parallelize(transform).toDF().show(transform.length)
      //  val sorted_model   = rdd.sortBy(elem => elem._1)

      //}

//    databaseuser.store(new User(UUIDs.timeBased(),
  //    transform.toList, List.empty[Double]))
    }
}
