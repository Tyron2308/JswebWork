package akkahelper

import Abstract.LogMetric
import modelAlgo.{Hyperparameter, hyper}
import akka.actor.{ActorSystem, Props}
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer

class EvaluateHelper(spark: SparkSession)
  extends EvaluaterHelpere[MatrixFactorizationModel, (Long, Array[(Long, Int)])]{


  /** a modifiter ici pour appliquer le build fraction qu'une seule fois !  **/
  def applyMetrics(ressource: Seq[Any],
                   namemodel: String,
                   hyperparameter: Hyperparameter)
                  (implicit mu: LogMetric[MatrixFactorizationModel])
  : Unit = {
    todebug("apply metric dans evaluateHelper")
    assert(ressource.size <= 3)
    try {
      todebug("....")
      val sys = ActorSystem("DispatchTraining")
      val fractionBuilder = sys.actorOf(Props[DispatchTraining]
        (new DispatchTraining(mu, ressource, spark, this,
          hyperparameter)))
      todebug(".....")
    }
    catch { case e: AssertionError => e.printStackTrace }
  }

  override def applyBatch(training: Vector[(Long, Array[(Long, Int)])],
                         lenght: Int, numberPerbatch: Int,
                         arraySample: ArrayBuffer[Vector[(Long, Array[(Long, Int)])]])
                           (implicit mu: LogMetric[MatrixFactorizationModel])
  : ArrayBuffer[Vector[(Long, Array[(Long, Int)])]] = {

      if (lenght < training.length) {
          val totake = training.take(numberPerbatch)
          val pd = arraySample :+ totake
          val len = pd.map { case (sequence) =>
            sequence.length
          }.sum
          applyBatch(training.drop(numberPerbatch), len, numberPerbatch, pd)
        } else arraySample
  }

  /*** throw assertion error a test ***/
  override def applyMetrics(ressource: Seq[Any], namemodel: String,
                            model: MatrixFactorizationModel,
                            callback: (MatrixFactorizationModel, Int, Int) => Array[Any],
                            numPartitions: Int, hyperameter: hyper)
                           (implicit mu: LogMetric[MatrixFactorizationModel])
  : Unit = {

    todebug("apply metric")
    try {
    assert(ressource.size >= 3)
    val traindata = ressource(0).asInstanceOf[Vector[(Long, Array[(Long, Int)])]]
    val sys = ActorSystem("Evaluatehelper")
    var arraySample = new ArrayBuffer[Vector[(Long, Array[(Long, Int)])]]()
    val batchs = applyBatch(traindata, 0, numPartitions, arraySample)
    todebug("apres avoir creeer des batchs" + batchs.size)
    for (elem <- batchs) {
      todebug("elem size : " + elem.size)
      elem match {
        case elem if (elem.length > 0) =>
          todebug("elem.lenght > 0")
          val fractionBuilder = sys.actorOf(Props[Dispacther]
              (new Dispacther(mu, Seq(ressource(1), ressource(2)), model, spark, this, namemodel, hyperameter)))
          fractionBuilder ! process2(elem, callback)
      }
    }
  } catch {
      case e: AssertionError => todebug("ASSERT ERROR ENMCU")
      case t: ClassCastException =>
        todebug(" Cast was bad dude apply metric  ")
    }
  }

}
