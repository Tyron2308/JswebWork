package helper

/**
  * Created by tyron on 25/07/17.
  */

trait utiles extends java.io.Serializable {

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
