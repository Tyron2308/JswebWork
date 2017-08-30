package Parsing

import scala.util.parsing.combinator._

trait Monoid[M] {
  def zero : M
  def op(a: M, b:M): M
}

object LogLexer extends RegexParsers {
  override val skipWhitespace           = false

  // Regex for the basic log line parsing
  def ip:           Parser[String] = """([^ ]*)""".r                                ^^ { _.toString }
  def unknown:      Parser[String] = """yes|no|\-""".r                              ^^ { _.toString }
  def request:      Parser[String] = """"([^"]*)"""".r                              ^^ { _.toString }
  def replyCode:    Parser[String] = """[0-9]{1,4} [0-9\-]{1,15}""".r               ^^ { _.toString }
  def taken:        Parser[String] = """PID ([0-9]*) TimeTaken ([0-9]*)""".r        ^^ { _.toString }
  def date:         Parser[String] = """[0-9]{2}/[a-zA-Z]{3}/[0-9]{4}""".r          ^^ { _.toString }
  def time:         Parser[String] = """[0-9]{2}:[0-9]{2}:[0-9]{2} \+[0-9]{4}""".r  ^^ { _.toString }
  def url:          Parser[String] = """[^"\\]*(?:\\.[^"\\]*)*""".r                 ^^ { _.toString }
  def fullUrl:      Parser[String] = "\"" ~> url <~ "\""                            ^^ { _.toString }
  def logLine:      Parser[List[String]] =
    ip ~ " " ~ unknown ~ " " ~ unknown ~ " [" ~ date ~ ":" ~ time ~ "] " ~ fullUrl ~ " " ~ replyCode ~ " " ~ fullUrl ~ " " ~ fullUrl ~ " " ~ taken ^^
    { case ip ~ _ ~ unknown1 ~ _ ~ unknown2 ~ _ ~ date ~ _ ~ time ~ _ ~ firstUrl ~ _ ~ repCode ~ _ ~ secondUrl ~ _ ~ device ~ _ ~ pid => List(ip, date, time, firstUrl, repCode, secondUrl, device, pid) }

  def transType:    Parser[String] = """[^"\\]*(?:\\.[^"\\]*)*""".r                 ^^ { _.toString }
  def metaType:     Parser[String] = """([a-zA-Z] """.r                             ^^ { _.toString }
  def metaContent:  Parser[String] = """ """.r                                      ^^ { _.toString }
  def trafic:       Parser[String] = """([^\?]*)\?()""".r                           ^^ { _.toString }
}

class LogParser extends java.io.Serializable {

  val schema        = List("ip", "date", "time","produit_id","url", "replyCode",
    "url2", "device", "debug", "reques", "boutique", "amount",
    "idcat", "produitid", "data", "idtransaction", "pname", "pcategory",
    "pproduct")
  val ERROR         = "error"
  val FAILURE       = "failure"
  val failureList   = schema.map(_ => FAILURE)
  val errorList     = schema.map(_ => ERROR)

  def createColumn(line: String): List[String] = {
    //TODO: get rid of the mutable variable
    var output = ""
    val parsed: List[String] = LogLexer.parse(LogLexer.logLine, line) match {
      case LogLexer.Success(matched, _) => matched
      case LogLexer.Failure(msg, _)     => output = msg; failureList
      case LogLexer.Error(msg, _)       => output = msg; errorList
    }

    List( parsed(0),
          parsed(1),
          parsed(2),
          extractRequestType(parsed(3)),
          parsed(3),
          parsed(4),
          parsed(5),
          parsed(6),
          if (parsed(0).equals(FAILURE)) output  else "Success")
  }

  def extractRequestType(req: String): String = {
    (req.equals(ERROR) || req.equals(FAILURE)) match {
      case true   =>  req
      case false  =>  val beginTypeIndex = "GET /".length
                      val endTypeIndex   = req.indexOf("/", beginTypeIndex)
                      val reqType = req.subSequence(beginTypeIndex, endTypeIndex).toString
                      reqType.startsWith("ee") match {
                        case true   => req.subSequence(beginTypeIndex, beginTypeIndex + 5).toString
                        case false  => reqType
                      }
    }
  }

  def todebug(str: String): Unit = {
    println(Console.GREEN + str)
    println(Console.WHITE)
  }

  def parse(rdd: Array[String]): Option[List[(String, List[String])]] = {

    def function(a: Array[((String, List[String]), Int)], b:  Array[((String, List[String]), Int)]) :
    Array[(String, List[String])] = {
      val lastmerge = a.map { elem =>
        val tmp = b.find(_._2 == elem._2)
        if (tmp.isDefined) {
          (elem._1._1, elem._1._2 ::: tmp.get._1._2) }
        else (elem._1._1, elem._1._2 ::: List())
      }
      lastmerge
    }

    val map = rdd.map(n => createColumn(n)).map(elem => (elem(0), elem))
    val lisp: Option[List[(String, List[String])]] = ParsingActorManager.initActor(map)
    val dataframe: Option[List[(String, List[String])]] = lisp.isDefined match {
      case true => {
        val merge = map.toSeq ++ lisp.get
        val grouped = merge.groupBy(_._1)
        val finale = grouped.map(elem => elem._2)
        val s = finale.filter(k => k.length == 2)
        val y = finale.filter(k => k.length > 2 && k.length % 2 == 0)
        val first = s.map(k => {
          (k(0)._1, k(0)._2 ::: k(1)._2)}).toArray
        val last = y.map(t => {
          val size = t.length / 2
          val bigurl = t.slice(0, size).zipWithIndex
          val smallurl = t.slice(size, t.length).zipWithIndex
          val end = function(bigurl.toArray, smallurl.toArray)
          end }).toArray.flatMap(k => k)
        val concat = first ++ last
        Some(concat.toList)
      }
      case false => println("FAILURE "); None
    }
    dataframe
  }
}

object LogParser extends LogParser 
