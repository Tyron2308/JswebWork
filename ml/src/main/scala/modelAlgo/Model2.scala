package modelAlgo
import Abstract.LogMetric
import Metric._
import Preprocessing.RetrieveInformation
import akkahelper.{EvaluateHelper, EvaluaterHelpere}
import com.outworkers.phantom.dsl._
import database.{AucDatabase, MetricDatabase, OverfitDatabase, UserDatabase}
import org.apache.spark.mllib.recommendation._
import org.apache.spark.rdd.RDD
import helper.utiles
import org.apache.parquet.filter2.predicate.Operators.UserDefined
import org.apache.spark.sql.SparkSession

object definition {
  final val CVSET = 1
  final val TRAINING = 0
}



/*** Modelsingleton est l object qui permet de gerer la logique de toute l'architecture.  ***/
/***
  RetrieveInformation : Object qui permeet a partir du DMP de creer la matrix de point correspodant au boutique visite par user
  Model : Encapsulation des models pour une utillisation plus simple par la suite.
  model.create() : creer la matrix de data point
  model.train(): permet d'entrainer le model retourne une RDD[Ratings] qui contient le produit pour l'user ainsi que la confiance de cette prediction
  model.runmetricOnModel permet de tester les differentes metric souhaite tant que cette metric respect l'heritage impose. (Logmetric)
  metric actuel : RMSE / AUC
  Overfiting: Object qui permet de valider avec le cvset si le modele est performant avec de la nouvelle data.


  Phase 1: runmetricmodel
***/
object ModelSingleton extends utiles {

  def pd(): Unit = {
    println("...")
  }

  @throws(classOf[RuntimeException])
  def toRun(spark: SparkSession): Unit = {
    val in = new RetrieveInformation(spark)
    todebug("wtf")
    in.matrixInt match {
      case Some(map) =>
        map.isEmpty match {
          case false =>
            val array = map.toVector
            val rddmatrixInt = spark.sparkContext
                                    .parallelize(array).cache()
            rddmatrixInt.count()
            val model        = new Model(spark)
            val sequence     = model.create(rddmatrixInt)
            val ratings      = model.trainData(rddmatrixInt)
                                    .asInstanceOf[RDD[Rating]]
            for (elem <- sequence) {
              elem.cache();
              elem.count()
            }
            rddmatrixInt.unpersist()
            val iterations = Seq(1)
            val ranks = Seq(10)
            val alpha = Seq(1.0)
            val reg = Seq(1.1)

            ratings.cache()
            val ratingscollected = ratings.collect()
            val hyperparameter = new Hyperparameter(iterations, 10, 1.1, 1.1, "model 0")
            val tmpranker = model.rankScoreModel(hyperparameter,
              ratings.asInstanceOf[RDD[Any]])
            val modelranker:
              MatrixFactorizationModel = tmpranker(0)._1._2
            val bromap = spark.sparkContext.broadcast(map.toArray)
            val rankerdic = tmpranker.map {
              elem => elem.swap
            }.toMap
            val sequencetoSend: Seq[Any] = Seq(array, in.bboutique, array)
            val helperEvalue = new EvaluateHelper(spark)
            val aucmetric = new AucMetric(AucDatabase.users, spark)
            val rmsemetric = new RmseMetric(MetricDatabase.users, spark)
            model.runMetricOnModel(rankerdic, helperEvalue)(aucmetric, sequencetoSend)
            model.runMetricOnModel(rankerdic, helperEvalue)(rmsemetric, sequencetoSend)

            todebug("OVERFIT")
   //         val pd = new OverfitingMetric(OverfitDatabase.users, spark)
 //       //    todebug("run on Metric")
       //     val toVector = sequence(definition.CVSET).collect().toVector
//
         //   val sequenceOverfiting: Seq[Any] = Seq(toVector,
           //   in.bboutique, toVector)
 //           pd.runMetricOnModel(rankerdic, helperEvalue)(pd, sequenceOverfiting)

     //       pd.loadValidedMode(in.indexBoutique, in.indexIp, UserDatabase.users)
            todebug("select dans database ===> ")
            ratings.unpersist()
            for (elem <- sequence) {
              elem.unpersist()
            }
            todebug("END ")
          case true =>
            throw new RuntimeException("e")
        }
      case None =>
        todebug("Get information fucked up.")
        throw new RuntimeException("none")
    }
  }
}



/*** Model extend ModelWrapper (interface avec quelques methodes) ***/
class Model(spark: SparkSession)
  extends java.io.Serializable
    with ModelWrapper[MatrixFactorizationModel,
                      (Long, Array[(Long, Int)])] {

  MetricDatabase.create()

  override def reloadmyModel(index: Int, path: String)
  : Option[MatrixFactorizationModel] = {
    println("<---- reload my model ---> ")
    None
  }

  override def create(matrix: RDD[(Long, Array[(Long, Int)])])
  : Seq[RDD[(Long, Array[(Long, Int)])]] = {
    (matrix.randomSplit(Array(0.7, 0.1, 0.2), 11L)).toSeq
  }

  override def trainData(matrix: RDD[(Long, Array[(Long, Int)])])
  : RDD[Any] = {

    val sequence = create(matrix)
    val training = sequence(0)
    val cvset = sequence(1)
    val valid = sequence(2)

    def scalerating(r: Rating): Rating = {
      val scaledRating = math.max(math.min(r.rating, 1.0), 0.0)
      Rating(r.user, r.product, scaledRating)
    }

    val scale = training.flatMap {
      case ((ip, product)) =>
        product.map {
          case (product, count) =>
            scalerating(Rating(ip.toInt, product.toInt, count))
        }
    }
    scale.asInstanceOf[RDD[Any]]
  }

  override def train(tofit: RDD[Any], hyperparam: hyper)
  : MatrixFactorizationModel = {

    val data = tofit.asInstanceOf[RDD[(Long, Array[(Long, Int)])]]
    val rating = data.flatMap {
      case (ip, p) =>
        p.map {
          case (pro, count) =>
            Rating(ip.toInt, pro.toInt, count.toDouble)
        }
    }

   // val rdd = spark.sparkContext.parallelize(rating)
    val hyperparameter = hyperparam.asInstanceOf[Hyperparameter]
    ALS.trainImplicit(rating,
      hyperparameter.rank,
      hyperparameter.iterationn.length,
      hyperparameter.alpha,
      hyperparameter.reg)

  }


  /** EvaluateHelper est une classe qui me permet grace aux multithread d'effectuer les metrics souhaite sur plusieurs batch en meme temps **/
  override def runMetricOnModel(mapModel: Map[String, (hyper, MatrixFactorizationModel)],
                       evalmaster: EvaluaterHelpere[MatrixFactorizationModel, (Long, Array[(Long, Int)])])
                      (implicit mu: LogMetric[MatrixFactorizationModel], seq: Seq[Any])

  : Unit = {
    try {
      val eval = evalmaster.asInstanceOf[EvaluateHelper]
      todebug("run Metric On Model.Model ")
      for (elem <- mapModel) {
        val hyperparam = elem._2._1
        eval.applyMetrics(seq, elem._1, elem._2._2,
          makemyPrediction, 100, hyperparam)(mu)
      }
    } catch {
      case e: ClassCastException => e.printStackTrace
    }
  }


  @throws
  override def makemyPrediction(model: MatrixFactorizationModel,
                                user: Int, number: Int)
  : Array[Any] = {
    model.recommendProducts(user, number).asInstanceOf[Array[Any]]
  }


  /*** rankScoremodel permet de savoir quel hyperparameter est efficace pour le model. A utiliser pour verifier les differents score obtenue ***/
  override def rankScoreModel(hyperparameter: hyper, rdd: RDD[Any])
  : Vector[((hyper, MatrixFactorizationModel), String)] = {
    try {
      val hyper = hyperparameter.asInstanceOf[Hyperparameter]

      val iteration = hyper.iterationn
      val rank      = Seq(hyper.rank)
      val alpha     = Seq(hyper.alpha)
      val lambdas   = Seq(hyper.reg)
      var index = 0
      val pd = for {
        elem <- iteration
        r <- rank
        a <- alpha
        lambda <- lambdas} yield {
        val model = ALS.trainImplicit(rdd.asInstanceOf[RDD[Rating]], r, elem, lambda, a)
        index = index + 1
        ((hyperparameter, model), "model " + index)
      }
      pd.toVector
    }
    catch {
      case e: ClassCastException =>
        e.printStackTrace
        Vector.empty
    }
  }

}


