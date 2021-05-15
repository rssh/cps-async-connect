package cps.monads.monix
/*
 * (C) Ruslan Shevchenko <ruslan@shevchenko.kiev.ua>
 * 2021
 */


import monix.eval.*
import monix.execution.*
import cps.*

import scala.util.*
import scala.concurrent.*

/**
 * CpsMonad for Monix Task
 **/
given MonixCpsMonad: CpsTryMonad[Task] with

  def pure[T](t:T): Task[T] = Task.pure(t)

  def map[A,B](fa:Task[A])(f: A=>B): Task[B] =
      fa.map(f)

  def flatMap[A,B](fa:Task[A])(f: A=>Task[B]): Task[B] =
      fa.flatMap(f)
  
  def error[A](e: Throwable): Task[A] =
      Task.raiseError[A](e)

  def flatMapTry[A,B](fa: Task[A])(f: Try[A]=>Task[B]): Task[B] =
      fa.materialize.flatMap(f)     


given futureToTask[T]:Conversion[Future[T],Task[T]] = (ft) => Task.fromFuture(ft)

given taskToFuture[T](using Scheduler): Conversion[Task[T],Future[T]] = _.runToFuture


given taskMemoization :CpsMonadInplaceMemoization[Task] with
    
  def apply[T](ft:Task[T]): Task[T] =
      ft.memoize

inline transparent given taskValueDiscard: ValueDiscard[Task[Unit]] = AwaitValueDiscard[Task,Unit]


