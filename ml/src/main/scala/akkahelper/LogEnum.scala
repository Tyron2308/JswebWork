package akkahelper

object LogEnum extends Enumeration {
  type LogEnum = Value
  val aucMetric   = Value("Metric.AucMetric")
  val rmseMetric  = Value("rmse")
  val default     = Value("default")
}
