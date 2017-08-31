import database._
import modelAlgo.ModelSingleton

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by tyron on 19/07/17.
  * Main principal pour lancer le singleton situe dans le package monAlgo.
  * Creation des databases
  */

object Main extends utiles {

  def main(array: Array[String]): Unit = {

    todebug(" creation des database - metrics")
    AucDatabase.create()
    MetricDatabase.create()
    DmpDatabase.create()
    OverfitDatabase.create()
    UserDatabase.create()
    todebug("run model ")

    try {
      ModelSingleton.isValild()
       ModelSingleton.runMetric(ModelSingleton.metrics)
      //  ModelSingleton.load()
      //ModelSingleton.runOverfit()
    }
    catch { case e: RuntimeException => todebug("runtime error while processing data") }
  }
}

