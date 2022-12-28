package probability_monad

import scala.util.Try


class MapTryDistribution[A,B](fa:Distribution[A],f:Try[A] => B) extends Distribution[B] {

  override def get: B = {
    f(Try(fa.get))
  }

}

class FlatMapTryDistribution[A,B](fa:Distribution[A],f:Try[A] => Distribution[B]) extends Distribution[B] {

  override def get: B = {
    f(Try(fa.get)).get
  }

}
