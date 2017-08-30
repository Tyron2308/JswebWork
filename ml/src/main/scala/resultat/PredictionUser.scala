package resultat

import akka.util.Timeout
import com.outworkers.phantom.dsl.ListResult
import database.{User, UserDatabase}
import databasehelper.{AucLog, OverfitLog}
import org.apache.spark.sql.SparkSession
import scala.concurrent.{Await, Future}

class PredictionUser(spark: SparkSession) {

  val tablestore  = UserDatabase.users
  val future      =  tablestore.myselect()
  val timeout     = new Timeout(500000)
  val result      = Await.result(future, timeout.duration)
                         .asInstanceOf[ListResult[User]]

  def displayUserPrediction(): Unit = {
    import spark.implicits._
    val listUser =      result.records
    spark.sparkContext.parallelize(listUser).toDF().show()
  }

  def updateDatabase(): Unit = {


  }

}
