package akkahelper

import Abstract.LogMetric
import helper.utiles
import modelAlgo.hyper

import scala.collection.mutable.ArrayBuffer

trait EvaluaterHelpere[Model, Datatouse]
  extends java.io.Serializable with utiles {

  case class process2(training: Vector[Datatouse],
                        modelcallback: (Model, Int, Int) => Array[Any])

  /*** separe une grosse matrix en plusieurs batch pour ensuite les envoyer a differents threads ***/
  def applyBatch(training: Vector[Datatouse],
                 lenght: Int, numberPerbatch: Int,
                 arraySample: ArrayBuffer[Vector[Datatouse]])
                (implicit mu: LogMetric[Model])
  : ArrayBuffer[Vector[Datatouse]]

  /*** la variable implicit est la metric a appliquer au predicton faite par le model - callback (parametre de la function)
    * etant la function qui permet d'appliquer la prediction ***/
   def applyMetrics(ressource: Seq[Any],
                    namemodel: String,
                    model: Model,
                    callback: (Model, Int, Int) => Array[Any],
                    numPartitions: Int,
                    hyperameter: hyper)(implicit mu: LogMetric[Model])
   : Unit
}
