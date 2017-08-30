package akkahelper

import org.apache.spark.broadcast.Broadcast

case class StartMeasure(sequence: Seq[Vector[Any]],
                         allads: Broadcast[Array[(Long, Int)]])
