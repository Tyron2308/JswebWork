package akkahelper

import Abstract.LogMetric
import modelAlgo.hyper
import akka.actor.Props
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.sql.SparkSession


/*** class qui permet d'attribuer des batchs de data a n'importe quel workerhelper disponible
  *
  */

class Dispacther(mu: LogMetric[MatrixFactorizationModel],
                 sequencedata: Seq[Any],
                 elemfrombatch: Any,
                 spark: SparkSession,
                 eval: EvaluateHelper,
                 modelname: String, hyper: hyper)
  extends java.io.Serializable with Dispatch {

   val allAds = sequencedata(0)
      .asInstanceOf[Broadcast[Array[(Long, Int)]]]
    val ratingslabel =
      sequencedata(1)
        .asInstanceOf[Vector[(Long, Array[(Long, Int)])]]

  override def receive: Receive = {
    case eval.process2(toFraction, callback) =>
      todebug("process receive")
      val (pospoint, negpoint) =
        mu.buildFraction(toFraction, allAds,
          elemfrombatch.asInstanceOf[MatrixFactorizationModel],
          callback, spark)
      todebug("build fraction, pospoint = Vector[Int, Array[Any]] ")
      todebug("rating label 2 =  Vector(Long, Array(Long, Int))")
      todebug("negpoint : Vector(Int, Vector(Int)")

      val sequence : Seq[Vector[Any]] = Seq(pospoint, negpoint,
        ratingslabel.asInstanceOf[Vector[Any]])

      self ! toWorkerHelper(sequence)

    case toWorkerHelper(seq) =>
      val pC  = context.actorOf(Props[WorkerHelper]
        (new WorkerHelper(mu,modelname, hyper, spark)))
      pC ! StartMeasure(seq, allAds)
  }
}
