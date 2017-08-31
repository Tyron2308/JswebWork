package akkahelper

import Abstract.LogMetric
import Metric.OverfitingMetric
import modelAlgo.Hyperparameter
import akka.actor.{ActorSystem, Props}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.SparkSession

class DispatchTraining(mu: LogMetric[MatrixFactorizationModel], sequencedata: Seq[Any],
                       spark: SparkSession, evaluateHelper:
                       EvaluaterHelpere[MatrixFactorizationModel, (Long, Array[(Long, Int)])],
                       hyperparameter: Hyperparameter)
  extends java.io.Serializable with Dispatch
{

  val training      = sequencedata(0).asInstanceOf[Vector[(Long, Array[(Long, Int)])]]
  val allAds        = sequencedata(1).asInstanceOf[Broadcast[Array[(Long, Int)]]]
  val rdd           = spark.sparkContext
                           .parallelize(training)
                           .cache()

  if (rdd.isEmpty())
    throw new RuntimeException("matrix in Dispatch training is empty")

  val nvxrating     = mu.asInstanceOf[OverfitingMetric].trainData(Some(rdd))
  for (elem <- 1 to 10) {
    self ! train(nvxrating, hyperparameter.name, sequencedata.drop(1))
  }

  override def receive: Receive = {

    case train(rat, name, sequence) =>

      val sys           = ActorSystem("WorkerTraining")
      val pd            = sys.actorOf(Props[WorkerTrainiing]
        (new WorkerTrainiing(mu, name, hyperparameter, spark, evaluateHelper)))
      pd ! fit(rat, sequence)
  }
}
