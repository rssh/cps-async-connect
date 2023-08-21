package cps.stream.pekko

import scala.concurrent.*
import org.apache.pekko
import pekko.NotUsed
import pekko.stream.*
import pekko.stream.scaladsl.*


import cps.*
import cps.monads.{*,given}
import cps.stream.{*,given}


given PekkoStreamEmitAbsorber[T](using ExecutionContext, Materializer):  BaseUnfoldCpsAsyncEmitAbsorber[Source[T,NotUsed],Future, FutureContext, T] with 

  override type Element = T

  def asSync(fs: Future[Source[T,NotUsed]]):Source[T,NotUsed] =
    Source.futureSource(fs).preMaterialize()._2

  def unfold[S](s0:S)(f:S => Future[Option[(T,S)]]): Source[T, NotUsed] =
        Source.unfoldAsync[S,T](s0)((s) => f(s).map(_.map{ case (x,y) => (y,x) }) )

