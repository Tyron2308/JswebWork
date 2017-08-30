package modelAlgo

case class Hyperparameter(iterationn: Seq[Int], rank: Int,
                          alpha: Double, reg: Double, name: String) extends hyper
