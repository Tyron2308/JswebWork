package databasehelper
import com.outworkers.phantom.dsl.{ListResult, UUID}
import database.AucDatabase

import scala.concurrent.Future

object definition {
  //  val keySpaceLog     = "worktest01"
  val keySpaceAuc     = "key23auc1"
  val keySpaceLog     = "test"
  val keySpaceMetric  = "key23rmse1"
  val keySpaceOverfit = "key23Overfit1"
  val keySpacemet     = "keyuser1"
}

trait Databaselog {
}

/*sealed  Log(timestamp: UUID,
                 modelname: String, good: Int,
                 hyperparam: List[Double],
                labelpred: List[((Long, Long), (Double, Double))])*/

case class RmseLog(id: UUID, modelname: String, good: Int,
                   hyperparam: List[Double],
                   labelpred: List[((Long, Long), (Double, Double))],
                   rootmean: Double)
  extends java.io.Serializable with Databaselog

case class AucLog(timestamp:  UUID, modelname: String, good: Int,
                  hyperparam: List[Double],
                  labelpred:  List[((Long, Long), (Double, Double))],
                  roclist: List[Double])
  extends java.io.Serializable with Databaselog

case class OverfitLog(timestamp:  UUID, modelname: String, good: Int,
                  hyperparam: List[Double],
                  labelpred:  List[((Long, Long), (Double, Double))],
                  ancien: Double, reel: Double)
  extends java.io.Serializable with Databaselog

