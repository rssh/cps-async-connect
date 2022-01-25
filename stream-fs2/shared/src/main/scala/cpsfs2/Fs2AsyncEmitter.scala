package cps.stream.fs2stream

import cps.*
import cps.stream.*
import scala.concurrent.*

given fs2EmitAbsorber[F[_],C <: CpsMonadContext[F],T](using ExecutionContext, CpsConcurrentMonad.Aux[F,C]):  BaseUnfoldCpsAsyncEmitAbsorber[fs2.Stream[F,T],F,C,T] with 

  override type Element = T

  def asSync(fs: F[fs2.Stream[F,T]]): fs2.Stream[F,T] =
        fs2.Stream.force(fs)

  def unfold[S](s0:S)(f:S => F[Option[(T,S)]]): fs2.Stream[F,T] =
        fs2.Stream.unfoldEval[F,S,T](s0)(f)

