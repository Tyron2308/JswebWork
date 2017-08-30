package database

import scala.concurrent.Future
import com.outworkers.phantom.dsl._

object DefaultConnector {
  val hosts = Seq("127.0.0.1")

  val connector = ContactPoints(hosts).keySpace("testTable")
}

object CassandraContainerConnector {
  val host = Seq("37.187.130.155", "51.255.86.136")
  val connector = ContactPoints(host).keySpace("Rawlogs")
}

class DmpDatabase(override val connector: CassandraConnection) extends Database[DmpDatabase](connector) {
  object users extends ConcreteLog with Connector
}
object DmpDatabase extends DmpDatabase(CassandraContainerConnector.connector)

abstract class Logs extends Table[Logs, Record] {
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

abstract class ConcreteLog extends Logs with RootConnector {
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
}

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
