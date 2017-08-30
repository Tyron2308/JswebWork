package akkahelper

import Abstract.LogMetric
import Metric.OverfitingMetric
import modelAlgo.Hyperparameter
import akka.actor.Actor
import helper.utiles
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.SparkSession

class WorkerTrainiing(mu: LogMetric[MatrixFactorizationModel],
                      modelname: String,
                      hyperparameter: Hyperparameter,
                      spark: SparkSession,
                      evaluateHelper:
                      EvaluaterHelpere[MatrixFactorizationModel, (Long, Array[(Long, Int)])])
  extends Actor with utiles
{
  override def receive: Receive = {
    case fit(nvxratings, sequence) =>
     val model = mu.asInstanceOf[OverfitingMetric]
         .train(nvxratings, hyperparameter)


     val collected = Seq(nvxratings.collect().toVector).asInstanceOf[Seq[Any]]

     val grouped = collected ++ sequence

      try {
        val deter = collected.asInstanceOf[Seq[(Long, Array[(Long, Int)])]]
        todebug("bon cast")
      }
      catch {
        case e : ClassCastException => todebug("mauvais cast")
      }

      todebug("size of coolected : " + collected.length)
      todebug("size of sequence : "+ sequence.length)
      todebug("size of grouped before assertion : " + grouped.length)

        evaluateHelper.applyMetrics(grouped, modelname, model,
       mu.asInstanceOf[OverfitingMetric].makemyPrediction, 100,
       hyperparameter)(mu)
  }
}
