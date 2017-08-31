package database
import com.outworkers.phantom.{ResultSet, dsl}
import com.outworkers.phantom.dsl._
import databasehelper.definition
import scala.concurrent.Future

case class Record (id: UUID,
                   ip: String,
                   date: String,
                   time: String,
                   requestType: String,
                   url: String,
                   replyCode: String,
                   url2: String,
                   device: String,
                   debug: String,
                   request:String,
                   boutique:String,
                   amount: String,
                   idcat: String,
                   piid:String,
                   data:String,
                   idtrans:String,
                   pproduit: String,
                   pname:String,
                   pcate:String)

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
  object id extends UUIDColumn with PartitionKey
  object ip extends StringColumn
  object date extends StringColumn
  object time extends StringColumn
  object requestType extends StringColumn
  object url extends StringColumn
  object replyCode extends StringColumn
  object url2 extends StringColumn
  object device extends StringColumn
  object debug extends StringColumn
  object request extends StringColumn
  object boutique extends StringColumn
  object amount extends StringColumn
  object idcat extends StringColumn
  object piid extends StringColumn
  object data extends StringColumn
  object idtrans extends StringColumn
  object pname extends StringColumn
  object pcate extends StringColumn
  object pproduit extends StringColumn

  def getById(id: UUID): Future[Option[Record]] = {
    select.where(_.id eqs id).one()
  }
}

abstract class ConcreteLog extends retargeting with RootConnector {
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

  def myselect(): Future[ListResult[Record]] = {
    select.all().fetchRecord()
  }
}

/*object MetricConnector {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts)
    .keySpace(definition.keySpaceMetric)
}

class MetricDatabase(override val connector : CassandraConnection)
  extends Database[MetricDatabase](connector) {
  object users extends ConcreteMetrics with Connector
}

object MetricDatabase
  extends MetricDatabase(MetricConnector.connector)

case class RmseLog1(id: UUID, modelname: String)

abstract class AMetrics extends Table[AMetrics, RmseLog1] {
  object id   extends UUIDColumn with PartitionKey
  object name extends StringColumn
  // object god extends IntColumn
  //  object hyper extends ListColumn[Double]
  // object label extends ListColumn[(Long, (Double, Double))]
  //object meansquare extends DoubleColumn

  override def fromRow(r: Row)
  : RmseLog1 = { RmseLog1(id(r), name(r)) }
}

abstract class ConcreteMetrics extends AMetrics with RootConnector {
  def store(metric : RmseLog1): Future[ResultSet] = {
    insert.value(_.id, metric.id)
          .value(_.name, metric.modelname)
      //.value(_.god, metric.good)
      //.value(_.hyper, metric.hyperparam.toList)
      //.value(_.meansquare, metric.rootmean)
      //.value(_.label, metric.labelpred.toList)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def myselect(): Future[ListResult[RmseLog1]] = {
    println("my select perfom query")
    select.all().fetchRecord()
  }
}*/
