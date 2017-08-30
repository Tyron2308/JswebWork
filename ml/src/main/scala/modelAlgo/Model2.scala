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
import org.apache.spark.sql.SparkSession
import scala.annotation.tailrec

/*** Modelsingleton est l object qui permet de gerer la logique du programme.  ***/
/***
  RetrieveInformation : Object qui permeet a partir du DMP de creer la matrix de point correspodant au boutique visite par user
  recuperer les logs, creer la matrix, transformer les boutiques/ip en id.

  Model : Encapsulation des models pour une utillisation plus simple par la suite. Cette classe etend elle meme ModelWrapper,
  une interface. A utiliser pour creer d'autre model et utilise les differents metric.

  model.create() : creer la matrix de data point depuis le DMP.
  model.train(): permet d'entrainer le modele, retourne une RDD[Ratings] qui
  contient le produit par user l'user (specifie le nombre de produit a recommander) ainsi que la confiance de cette prediction

  model.runmetricOnModel permet de tester les differentes metric souhaite tant que cette metric respect l'heritage impose. (Logmetric)
  metric actuel : RMSE / AUC
  Overfiting: Object qui permet de valider avec le cvset si le modele est performant avec de la nouvelle data.

  Phase 1: runmetricmodel
***/

object definition {
  final val CVSET = 1
  final val TRAINING = 0
}

object ModelSingleton extends utiles {

  val spark: SparkSession = SparkSession.builder()
    .appName("machine learnig")
    .config("spark.master", "local")
    .getOrCreate()

  val (information, rddmatrixInt) = createMatrixDatapoint()
  val metrics = Vector(new AucMetric(AucDatabase.users, spark),
    new RmseMetric(MetricDatabase.users, spark))

  val model = new Model(spark)
  val sequence = model.create(rddmatrixInt.get)
  val ratings = model.trainData(rddmatrixInt.get)
    .asInstanceOf[RDD[Rating]]

  for (elem <- sequence) {
    elem.cache();
    elem.count()
  }

  /** * rank score model est fait pour remplacer ces lignes ***/
  rddmatrixInt.get.unpersist()

  @throws(classOf[RuntimeException])
  def isValild(): Unit = {
    rddmatrixInt.isDefined match {
      case true => todebug("rdd is okay")
      case false => throw new RuntimeException(" matrix empty abort ml.")
    }
  }


  def runtestOnModel(): Vector[((hyper, MatrixFactorizationModel), String)] = {

    todebug(" runtestonModel ")
    val iterations = Seq(1)
    val ranks = Seq(10)
    val alpha = Seq(1.0)
    val reg = Seq(1.1)
    ratings.cache()
    val ratingscollected = ratings.collect()
    val hyperparameter = new Hyperparameter(iterations, 10, 1.1, 1.1, "model 0")
    model.rankScoreModel(hyperparameter, ratings.asInstanceOf[RDD[Any]])
  }

  val rankings = runtestOnModel()
  val modelranker: MatrixFactorizationModel = rankings(0)._1._2
  val bromap = spark.sparkContext.broadcast(rddmatrixInt.get.collect())
  val rankerdic = rankings.map { elem => elem.swap }.toMap

  val vectorsend = rddmatrixInt.get.collect().toVector
  val sequencetoSend: Seq[Any] = Seq(vectorsend, information.bboutique, vectorsend)
  val helperEvalue = new EvaluateHelper(spark)


  def createMatrixDatapoint():
  (RetrieveInformation, Option[RDD[(Long, Array[(Long, Int)])]]) = {
    val in = new RetrieveInformation(spark)
    val array = in.matrixInt.get.toVector

    array.length match {
      case x if (x > 0) =>
        (in, Some(spark.sparkContext.parallelize(array).cache()))
      case _ =>
        (in, None)
    }
  }

   ratings.unpersist()
    for (elem <- sequence) {
      elem.unpersist()
    }

  @tailrec
  def runMetric(metrics: Vector[LogMetric[MatrixFactorizationModel]]): Unit =
    (metrics) match {
      case x if (x.length > 0) =>
        model.runMetricOnModel(rankerdic, helperEvalue)(x.head, sequencetoSend)
        runMetric(x.tail)
      case _ =>
        todebug("plus de metric a faire evaluer.")
    }

  @throws(classOf[RuntimeException])
  def runOverfit(): OverfitingMetric = {
    val pd = new OverfitingMetric(OverfitDatabase.users, spark)
    todebug("run on Metric")
    val toVector = sequence(definition.CVSET).collect().toVector
    val sequenceOverfiting: Seq[Any] = Seq(toVector, information.bboutique, toVector)
    pd.runMetricOnModel(rankerdic, helperEvalue)(pd, sequenceOverfiting)
    pd
  }

  @throws(classOf[RuntimeException])
  def load(): Unit = {
    new OverfitingMetric(OverfitDatabase.users, spark)
      .loadValidedMode(information.indexBoutique, information.indexIp, UserDatabase.users)
  }

  todebug("select dans database ===> ")
  ratings.unpersist()
  for (elem <- sequence) {
    elem.unpersist()
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

  /*** train data   ***/
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


  /**
      EvaluateHelper est une classe qui me permet grace
      aux multithread d'effectuer les metrics souhaite sur plusieurs batch en meme temps
    **/

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


