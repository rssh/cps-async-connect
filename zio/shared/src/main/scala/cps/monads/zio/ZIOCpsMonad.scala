package cps.monads.zio

import cps._
import zio._
import scala.util._
import scala.concurrent._

class ZIOCpsMonad[R,E](using ThrowableAdapter[R,E]) extends CpsAsyncMonad[[X]=>>ZIO[R,E,X]]:

  type F[T] = ZIO[R,E,T]

  def pure[A](x:A):ZIO[R,E,A] = ZIO.succeed(x)

  def map[A,B](fa: F[A])(f: A=>B): F[B] =
      fa.map(f)

  def flatMap[A,B](fa: F[A])(f: A=> F[B]): F[B] =
      fa.flatMap(f)

  def error[A](e: Throwable): F[A] = 
      summon[ThrowableAdapter[R,E]].fromThrowable[A](e)

  def flatMapTry[A, B](fa: F[A])(f: util.Try[A] => F[B]): F[B] =
      fa.foldM(
          e => f(Failure(summon[ThrowableAdapter[R,E]].toThrowable(e))),
          a => f(Success(a))
      )
           
  def adoptCallbackStyle[A](source: (util.Try[A] => Unit) => Unit): F[A] =
      def adoptZIOCallback(zioCallback: ZIO[R,E,A]=>Unit): Try[A]=>Unit = {
         case Failure(ex) => zioCallback(error(ex))
         case Success(a) => zioCallback(ZIO.succeed(a))
      }
      ZIO.effectAsync[R,E,A] { cb =>
         source(adoptZIOCallback(cb))      
      }


  def throwableAdaper: ThrowableAdapter[R,E] =
      summon[ThrowableAdapter[R,E]]



object TaskCpsMonad extends ZIOCpsMonad[Any,Throwable]

given CpsAsyncMonad[Task] = TaskCpsMonad

given rioCpsMonad[R] : CpsAsyncMonad[[X]=>>RIO[R,X]] = ZIOCpsMonad[R,Throwable]

given zioCpsMonad[R,E](using ThrowableAdapter[R,E]): CpsAsyncMonad[[X]=>>ZIO[R,E,X]] = ZIOCpsMonad[R,E]

transparent inline def asyncZIO[R,E](using CpsAsyncMonad[[X]=>>ZIO[R,E,X]]): Async.InferAsyncArg[[X]=>>ZIO[R,E,X]] =
   new Async.InferAsyncArg[[X]=>>ZIO[R,E,X]]

transparent inline def asyncRIO[R]: Async.InferAsyncArg[[X]=>>RIO[R,X]] =
   new Async.InferAsyncArg[[X]=>>RIO[R,X]](using ZIOCpsMonad[R, Throwable])

given zioThrowableToE[R1,R2 <: R1, ET <: Throwable, E](using ThrowableAdapter[R2,E]): 
                           CpsMonadConversion[[X]=>>ZIO[R1,ET,X], [X]=>>ZIO[R2, E, X]] with

   override def apply[T](mf: CpsMonad[[X]=>>ZIO[R1,ET,X]], mg: CpsMonad[[X]=>>ZIO[R2,E,X]], ft:ZIO[R1,ET,T]): ZIO[R2,E,T] =
        ft.foldM(
          ex => summon[ThrowableAdapter[R2,E]].fromThrowable(ex),
          v => ZIO.succeed(v)
        )



given futureZIOConversion[R, E](using zio.Runtime[R], ThrowableAdapter[R,E]):CpsMonadConversion[[X]=>>ZIO[R,E,X],Future] with

   override def apply[T](mf: CpsMonad[[X]=>>ZIO[R,E,X]], mg: CpsMonad[Future], ft:ZIO[R,E,T]): Future[T]  =
        summon[Runtime[R]].unsafeRunToFuture(ft.mapError(e => summon[ThrowableAdapter[R,E]].toThrowable(e)))


given zioMemoization[R,E]: CpsMonadPureMemoization[[X]=>>ZIO[R,E,X]] with 

   def apply[T](ft:ZIO[R,E,T]):ZIO[R,E, ZIO[R,E,T]] =
      ft.memoize


//inline transient