package database
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.ListResult
import com.outworkers.phantom.dsl.{CassandraConnection, ConsistencyLevel, ContactPoints, Database, PartitionKey, RootConnector, Table, UUID}
import databasehelper.{RmseLog, definition}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MetricConnector {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts)
    .keySpace(definition.keySpaceMetric)
}

object CassandraContainerConnector3 {
  val host = Seq("37.187.130.155", "51.255.86.136")
  val connector = ContactPoints(host).keySpace(definition.keySpaceMetric)
}

class MetricDatabase(override val connector : CassandraConnection)
  extends Database[MetricDatabase](connector) {
  object users extends ConcreteMetrics with Connector
}

//object MetricDatabase extends MetricDatabase(CassandraContainerConnector3.connector)
object MetricDatabase extends MetricDatabase(MetricConnector.connector)

case class RmseLog1(id: UUID, modelname: String)

/*** name: modelname -  god : good or bad predictor -
  * hyper : hyperparameter - label : prediction id product confiance ratings
  * meansquare : root means square error
  *
  */
abstract class AMetrics extends Table[AMetrics, RmseLog1] {
  object id   extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object god extends IntColumn
  object hyper extends ListColumn[Double]
  object label extends ListColumn[((Long, Long), (Double, Double))]
  object meansquare extends DoubleColumn
 def getById(id: UUID): Future[Option[RmseLog1]] = {
   select.where(_.id eqs id).one()
 }
}

abstract class ConcreteMetrics extends AMetrics with RootConnector {
  def store(metric : RmseLog): Future[ResultSet] = {
    insert.value(_.id, metric.id)
          .value(_.name, metric.modelname)
          .value(_.god, metric.good)
          .value(_.hyper, metric.hyperparam)
          .value(_.meansquare, metric.rootmean)
          .value(_.label, metric.labelpred)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def myselect(): Future[ListResult[RmseLog1]] = {
      println("my select perfom query")
      select.all().fetchRecord()

    }
}
