import scala.concurrent.{Await, ExecutionException}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import com.datastax.driver.core.utils.UUIDs
import database._
import databasehelper.AucLog



trait utiles {

  def todebug(dtr: String*): Unit = {
    println(Console.GREEN + dtr(0))
    println(Console.WHITE)
  }

  def time[P, T, R](body: => P, str: T*)(f: (T*) => R): Unit = {
    val t0 = System.currentTimeMillis()
    val ressource = body
    f(str :_*)
    val t1 = System.currentTimeMillis()
    todebug("duratiion between t0 - t1 : " + (t1 - t0))
  }
}

object MainDatabase extends utiles {

  def main(args: Array[String]): Unit = {

    DmpDatabase.create()
    MetricDatabase.create()
    AucDatabase.create()

    val test = ((0.toLong, (0.0, 0.0)))

    val enculte = AucDatabase.users.myselect()
    val timeout = new Timeout(500000)
    val result = Await.result(enculte, timeout.duration)
    val second = Await.result(MetricDatabase.users.myselect(), timeout.duration)

    println("<--- affiche database1  ---> ")
    result.records.foreach { case (reco) =>
      todebug(reco.modelname)
        todebug(reco.hyperparam.mkString("- "))
        todebug(reco.roclist.mkString("-"))
    }

    todebug("affiche rmse databse")
    second.records.foreach {
      case (rmse) => todebug(rmse.modelname)
    }
    todebug("affiche databse 2")
 }
}
