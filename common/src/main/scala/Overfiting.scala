package database
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.ListResult
import com.outworkers.phantom.dsl.{CassandraConnection, ConsistencyLevel, ContactPoints, Database, PartitionKey, RootConnector, Table}

import scala.concurrent.ExecutionContext.Implicits.global
import databasehelper.{AucLog, OverfitLog, definition}

import scala.concurrent.{ExecutionContext, Future}

object OverfitConnector {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts)
    .keySpace(definition.keySpaceOverfit)
}

object CassandraContainerConnector4 {
  val host = Seq("37.187.130.155", "51.255.86.136")
  val connector = ContactPoints(host).keySpace(definition.keySpaceOverfit)
}
class OverfitDatabase(override val connector : CassandraConnection)
  extends Database[OverfitDatabase](connector) {
  object users extends OverfitMetrics with Connector
}

object OverfitDatabase extends OverfitDatabase(OverfitConnector.connector)
//bject OverfitDatabase extends OverfitDatabase(CassandraContainerConnector4.connector)


/*** name : id du modele. ud: prediction from good model or bad model, hyperparam : hyperarameter du modele
     lab: user prediction confiance confiacne
     ancien : metric trainingdata reel: metric cvset

     mettre ces deux dernieres en vector et rajouter plus de metrics a ce vector
  ***/

abstract class OverfitMetric extends Table[OverfitMetrics, OverfitLog] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object ud extends IntColumn
  object hyper extends ListColumn[Double]
  object lab extends ListColumn[((Long, Long), (Double, Double))]
  object ancien extends DoubleColumn
  object reel extends DoubleColumn
}

abstract class OverfitMetrics extends OverfitMetric with RootConnector {
  def store(metric : OverfitLog): Future[ResultSet] = {

    insert.value(_.id, metric.timestamp)
      .value(_.name, metric.modelname)
      .value(_.ud, metric.good)
      .value(_.hyper, metric.hyperparam)
      .value(_.ancien, metric.ancien)
      .value(_.reel, metric.reel)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def myselect(): Future[ListResult[OverfitLog]] = {
    select.all().fetchRecord()
  }
}
