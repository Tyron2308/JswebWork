package database
import com.outworkers.phantom.{ResultSet, dsl}
import com.outworkers.phantom.dsl._
import databasehelper.definition
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.concurrent.Future

case class Record (id: UUID,
                   ip: String,
                   date: String,
                   time: String,
                   category_id: String,
                   product_id : String,
                   ref: String,
                   requestype: String,
                   shop: String,
                   transaction_amount: String,
                   transaction_id: String)


object DefaultConnector {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts)
    .keySpace(definition.keySpaceLog)
}

object CassandraContainerConnector {
  val host = Seq("37.187.130.155", "51.255.86.136")
  val connector = ContactPoints(host).keySpace("test")
}

class DmpDatabase(override val connector: CassandraConnection)
  extends Database[DmpDatabase](connector) {
  object users extends ConcreteLog with Connector
}

//object DmpDatabase extends DmpDatabase(DefaultConnector.connector)
object DmpDatabase extends DmpDatabase(CassandraContainerConnector.connector)

abstract class retargeting extends Table[retargeting, Record] {
  object id           extends UUIDColumn with PartitionKey
  object ip           extends StringColumn
  object date         extends StringColumn
  object time         extends StringColumn
  object category_id  extends StringColumn
  object product_id   extends StringColumn
  object ref          extends StringColumn
  object requesttype  extends StringColumn
  object shop         extends StringColumn
  object transaction_amount extends StringColumn
  object transaction_id extends StringColumn

  def getById(id: UUID): Future[Option[Record]] = {
    select.where(_.id eqs id).one()
  }
}


abstract class ConcreteLog extends retargeting with RootConnector {
  /*
  def store(rec: Record): Future[ResultSet] = {
    insert
      .value(_.id, rec.id)
      .value(_.ip, rec.ip)
      .value(_.date, rec.date)
      .value(_.time, rec.time)
      .value(_.requestType, rec.requestType)
      .value(_.url, rec.url)
      .value(_.replyCode, rec.replyCode)
      .value(_.url2, rec.url2)
      .value(_.device, rec.device)
      .value(_.debug, rec.debug)
      .value(_.request, rec.request)
      .value(_.boutique, rec.boutique)
      .value(_.amount, rec.amount)
      .value(_.idcat, rec.idcat)
      .value(_.piid, rec.piid)
      .value(_.data, rec.data)
      .value(_.idtrans, rec.idtrans)
      .value(_.pcate, rec.pcate)
      .value(_.pproduit, rec.pproduit)
      .value(_.pname, rec.pname)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
*/

  def myselect(spark: SparkSession): RDD[(String, String)] = {

    import com.datastax.spark.connector._

    val rdd = spark.sparkContext.cassandraTable("test", "retargeting")
    val ip = rdd.map(elem => elem.getString("shop"))

    val boutique = rdd.map(lel => lel.getInet("ip"))

//    boutique.map(_.toString)
    val test =     (ip.zipWithIndex()
      ++ boutique.map(_.toString).zipWithIndex()).groupBy(_._2)
    val finale =    test.map {
      elem =>
        val string1 = elem._2.head._1
        val str = elem._2.last._1
        (string1, str)
    }
    finale
//    select.all().fetchRecord()
  }
}


