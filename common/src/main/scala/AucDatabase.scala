package database
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.ListResult
import com.outworkers.phantom.dsl.{CassandraConnection, ConsistencyLevel, ContactPoints, Database, PartitionKey, RootConnector, Table}
import databasehelper.{AucLog, definition}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionException, Future}

object AucConnector {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts)
    .keySpace(definition.keySpaceAuc)
}

object CassandraContainerConnector1 {
  val host = Seq("37.187.130.155", "51.255.86.136")
  val connector = ContactPoints(host).keySpace(definition.keySpaceAuc)
}

class AucDatabase(override val connector : CassandraConnection)
  extends Database[AucDatabase](connector) {
  object users extends CMetrics with Connector
}

object AucDatabase extends AucDatabase(CassandraContainerConnector1.connector)
//object AucDatabase extends AucDatabase(AucConnector.connector)

abstract class AucMetrics extends Table[AucMetrics, AucLog] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object ud extends IntColumn
  object zob extends ListColumn[Double]
  object hyper extends ListColumn[Double]
  object lab extends ListColumn[((Long, Long), (Double, Double))]
}

abstract class CMetrics extends AucMetrics with RootConnector {
  def store(metric : AucLog): Future[ResultSet] = {

    insert.value(_.id, metric.timestamp)
      .value(_.name, metric.modelname)
      .value(_.ud, metric.good)
      .value(_.zob, metric.roclist)
      .value(_.hyper, metric.hyperparam)
      .value(_.lab, metric.labelpred)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def myselect(): Future[ListResult[AucLog]] = {
    select.all().fetchRecord()
  }
}


