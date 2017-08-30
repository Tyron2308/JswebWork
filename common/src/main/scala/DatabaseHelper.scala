package databasehelper
import com.outworkers.phantom.dsl.{ListResult, UUID}
import database.AucDatabase

import scala.concurrent.Future

/*** singleton pour les differents keyspace des databases ***/
object definition {
  val keySpaceAuc     = "key23auc1"
  val keySpaceLog     = "pd"
  val keySpaceMetric  = "key23rmse1"
  val keySpaceOverfit = "key23Overfit1"
  val keySpacemet     = "keyuser1"
}

/*object definition {
  //  val keySpaceLog     = "worktest01"
  val keySpaceAuc     = "key23auc1"
  val keySpaceLog     = "test"
  val keySpaceMetric  = "key23rmse1"
  val keySpaceOverfit = "key23Overfit1"
  val keySpacemet     = "keyuser1"
}*/


/***les case classe pour le schema des databases  ***/
trait Databaselog {
}

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

