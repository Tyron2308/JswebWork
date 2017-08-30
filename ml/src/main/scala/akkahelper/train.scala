package akkahelper

import org.apache.spark.rdd.RDD

case class train(n: RDD[Any], name: String, sequence: Seq[Any])
