package akkahelper

import Abstract.LogMetric
import Metric.{AucMetric, OverfitingMetric, RmseMetric}
import modelAlgo.hyper
import akka.actor.Actor
import helper.utiles
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.SparkSession
import shapeless.TypeCase

class WorkerHelper(mu: LogMetric[MatrixFactorizationModel],
                   modelname: String, hyper: hyper,
                   spark: SparkSession) extends Actor with utiles {

  val Aucmetric         = TypeCase[AucMetric]
  val rmseMetric        = TypeCase[RmseMetric]
  val overfitingMetric  = TypeCase[OverfitingMetric]

  override def receive: Receive = {

    case StartMeasure(seq, ads) =>
      todebug("start measure for metric")
      assert(seq.size >= 2)
      mu match {
        case Aucmetric(auc) =>
          auc.myMetricFunction(seq, ads, modelname, hyper, spark)
        case rmseMetric(rmse) =>
          rmse.myMetricFunction(seq, ads, modelname, hyper, spark)
        case overfitingMetric(mex) =>
          mex.myMetricFunction(seq, ads, modelname, hyper, spark)
      }
  }
}
