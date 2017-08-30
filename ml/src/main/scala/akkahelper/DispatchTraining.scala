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

  todebug("....3")
  val training      = sequencedata(0)
    .asInstanceOf[Vector[(Long, Array[(Long, Int)])]]

  val allAds        = sequencedata(1).asInstanceOf[Broadcast[Array[(Long, Int)]]]
//  val ratingslabel  = sequencedata(2).asInstanceOf[Vector[Any]]

  /** enlever un cache **/
  val rdd           = spark.sparkContext
                           .parallelize(training)
                           .cache()

  todebug("....t")
  val nvxrating     = mu.asInstanceOf[OverfitingMetric].trainData(rdd)

//  nvxrating

  for (elem <- 1 to 10) {
    self ! train(nvxrating, hyperparameter.name, sequencedata.drop(1))
  }

  todebug("here apres avoiir call 10 x train")
  //self ! train(null, null, null)
  override def receive: Receive = {

    case train(rat, name, sequence) =>


      todebug("TRAINNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")
      println("TRAIN BABE")

      println("TRAIN BABE")
      val sys           = ActorSystem("WorkerTraining")
      val pd            = sys.actorOf(Props[WorkerTrainiing]
        (new WorkerTrainiing(mu, name, hyperparameter, spark, evaluateHelper)))
      pd ! fit(rat, sequence)
  }
}
