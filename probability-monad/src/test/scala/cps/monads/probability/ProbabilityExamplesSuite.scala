package cps.monads.probability


import cps.*
import probability_monad.*
import Distribution.*

import munit.*

class ProbabilityExamplesSuite extends FunSuite {

  case class Trial(haveFairCoin: Boolean, flips: List[Coin])

  def bayesianCoin(nFlips: Int): Distribution[Trial] = reify[Distribution] {
       val haveFairCoin = reflect(tf())
       val myCoin = if (haveFairCoin) coin else biasedCoin(0.9)
       val flips = reflect(myCoin.repeat(nFlips))
       Trial(haveFairCoin, flips)
  }

  test("basic coin test") {
    val p = bayesianCoin(5).filter(_.flips.forall(_ == H)).pr(_.haveFairCoin)
    //println(s"p=$p")
    assert(p < 0.1)
  }

}

