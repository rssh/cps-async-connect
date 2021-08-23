package cps.stream.akka

import scala.concurrent.*
import akka.NotUsed
import akka.stream.*
import akka.stream.scaladsl.*


import cps.*
import cps.monads.{*,given}
import cps.stream.{*,given}


given AkkaStreamEmitAbsorber[T](using ExecutionContext):  BaseUnfoldCpsAsyncEmitAbsorber[Source[T,NotUsed],Future,T] with 

  override type Element = T

  def unfold[S](s0:S)(f:S => Future[Option[(T,S)]]): Source[T, NotUsed] =
        Source.unfoldAsync[S,T](s0)((s) => f(s).map(_.map{ case (x,y) => (y,x) }) )

