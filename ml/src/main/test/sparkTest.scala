/*
import org.apache.spark.SparkContext

case class WordCount(str: String, count: Int)

object WordCount {
      def count(str: String): RDD[WorkCount] = {
            val w = new WordCount(str, str.split(" ").length)
      }
}

class SparkSpec extends FlatSpec with BeforeAndAfter {
      private val master = "local[2]"
      private val appname = "spark-test"

      before {
            val conf = new SparkConf().setMaster(master).setAppname(appname)
            val sc = new SparkContext(conf)
            val lines = Seq("run a test with spark", "run an another test with scala")
            val rdd = sc.parallelize(lines)
            val container = mutable.Queue[RDD[String]]()

            container += sc.makeRDD(lines)
      }

      after {
            if (sc != null) sc.stop()
      }
}*/
