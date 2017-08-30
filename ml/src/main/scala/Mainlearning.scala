import database._
import helper.utiles
import modelAlgo.ModelSingleton
import org.apache.spark.sql.SparkSession
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by tyron on 19/07/17.
  * Main principal pour lancer le singleton situe dans le package monAlgo.
  * Creation des databases
  */

object Main extends utiles {

  val spark: SparkSession = SparkSession.builder()
    .appName("machine learnig")
    .config("spark.master", "local")
    .getOrCreate()

  def main(array: Array[String]): Unit = {

    AucDatabase.create()
    MetricDatabase.create()
    DmpDatabase.create()
    OverfitDatabase.create()
    UserDatabase.create()

    todebug("run model ")

    try { ModelSingleton.toRun(spark) } catch {
      case e: RuntimeException => todebug("toRun from  model return exception") }

  }
}

