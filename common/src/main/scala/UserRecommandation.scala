package database
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.dsl.{CassandraConnection, ConsistencyLevel, ContactPoints, Database, ListResult, PartitionKey, RootConnector, Table, UUID}
import databasehelper.definition

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/*** user remcommandation est la database qui dispose des users deja rencontre et des prediction a renvoye pour le RTB -\
manque les functions d'update
  **/

object DefaultConnectorUser {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts).keySpace(definition.keySpacemet)
}

class UserDatabase(override val connector: CassandraConnection)
  extends Database[UserDatabase](connector) {
  object users extends UserRecommandation with Connector
}

object CassandraContainerConnector2 {
  val host = Seq("37.187.130.155", "51.255.86.136")
  val connector = ContactPoints(host).keySpace(definition.keySpacemet)
}

object UserDatabase extends UserDatabase(CassandraContainerConnector2.connector)
//object UserDatabase extends UserDatabase(DefaultConnectorUser.connector)

abstract class Usermet extends Table[Usermet,User] {
  object id extends UUIDColumn with PartitionKey
  object user extends LongColumn
  object product extends ListColumn[(String, String)]
  object confiance extends ListColumn[Double]

  def getById(id: UUID): Future[Option[User]] = {
    select.where(_.id eqs id).one()
  }
}

case class User(timestamp: UUID, ref: List[(String, String)], rate: List[Double])

abstract class UserRecommandation extends Usermet with RootConnector {
  def store(rec: User): Future[ResultSet] = {
    insert.value(_.id, rec.timestamp)
          .value(_.product, rec.ref)
          .value(_.confiance, rec.rate)
          .consistencyLevel_=(ConsistencyLevel.ONE)
          .future()
  }

  def myselect(): Future[ListResult[User]] = {
    select.all().fetchRecord()
  }
}
