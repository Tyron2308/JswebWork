package modelAlgo

import Abstract.LogMetric
import akkahelper.EvaluaterHelpere
import helper.utiles
import org.apache.spark.rdd.RDD

trait ModelWrapper[Modeluse, Datatype] extends utiles {

  @throws
  def makemyPrediction(model: Modeluse, user: Int, number: Int)
  : Array[Any]

  @throws(classOf[RuntimeException])
  def train(tofit: RDD[Any], hyperparam: hyper)
  : Modeluse

  def reloadmyModel(index: Int, path: String)
  : Option[Modeluse]

  def create(matrix: Option[RDD[(Long, Array[(Long, Int)])]])
  : Seq[RDD[Datatype]] //= {

  @throws(classOf[RuntimeException])
  def trainData(matrix: Option[RDD[Datatype]])
  : RDD[Any]

  def rankScoreModel(hyperparameter: hyper, rdd: RDD[Any])
  : Vector[((hyper, Modeluse), String)]

  def runMetricOnModel(mapModel: Map[String, (hyper, Modeluse)],
                       evalmaster: EvaluaterHelpere[Modeluse, Datatype])
                      (implicit mu: LogMetric[Modeluse], seq: Seq[Any])

  : Unit

}
