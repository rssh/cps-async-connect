package cps.stream.monix

import cps.monads.monix.{*,given}

import monix.eval.*
import monix.execution.*
import monix.reactive.*

import cps.*
import cps.stream.*
import scala.concurrent.*

given ObservableEmitAbsorber[T](using ec: ExecutionContext):  BaseUnfoldCpsAsyncEmitAbsorber[Observable[T],Task, CpsMonadInstanceContextBody[Task], T] with 

  override type Element = T

  def asSync(task: Task[Observable[T]]): Observable[T] =
        Observable.fromTask(task).flatten


  def unfold[S](s0:S)(f:S => Task[Option[(T,S)]]):Observable[T] =
        Observable.unfoldEval[S,T](s0)(f)