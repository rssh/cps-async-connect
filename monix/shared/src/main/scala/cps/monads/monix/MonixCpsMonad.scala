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
given MonixCpsMonad: CpsConcurrentEffectMonad[Task] with CpsMonadInstanceContext[Task] with

  override type Spawned[A] = Fiber[A]

  def pure[T](t:T): Task[T] = Task.pure(t)

  def map[A,B](fa:Task[A])(f: A=>B): Task[B] =
      fa.map(f)

  def flatMap[A,B](fa:Task[A])(f: A=>Task[B]): Task[B] =
      fa.flatMap(f)
  
  def error[A](e: Throwable): Task[A] =
      Task.raiseError[A](e)

  def flatMapTry[A,B](fa: Task[A])(f: Try[A]=>Task[B]): Task[B] =
      fa.materialize.flatMap(f)     

  def adoptCallbackStyle[A](source: (Try[A]=>Unit)=>Unit): Task[A] =
      Task.async{ 
         callback => source(r => callback.apply(r))
      }

  def spawnEffect[A](op: => Task[A]) =
     op.start

  def join[A](fiber: Fiber[A]) = fiber.join

  def tryCancel[A](op: Fiber[A]): Task[Unit] =
    op.cancel




given futureToTask: CpsMonadConversion[Future,Task] with
    def apply[T](ft: Future[T]): Task[T] = Task.fromFuture(ft)

given taskToFuture(using Scheduler): CpsMonadConversion[Task,Future] with
    def apply[T](ft: Task[T]):Future[T] = ft.runToFuture



