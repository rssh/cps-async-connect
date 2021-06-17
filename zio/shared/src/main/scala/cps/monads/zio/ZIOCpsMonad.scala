package cps.monads.zio

import cps._
import zio._
import scala.util._
import scala.concurrent._

class ZIOCpsMonad[R, E](using ThrowableAdapter[R,E]) extends CpsAsyncEffectMonad[[X]=>>ZIO[R,E,X]]:

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

given CpsAsyncEffectMonad[Task] = TaskCpsMonad

//given rioCpsMonad[R] : CpsAsyncMonad[[X]=>>RIO[R,X]] = ZIOCpsMonad[R,Throwable]

given zioCpsMonad[R,E](using ThrowableAdapter[R,E]): ZIOCpsMonad[R,E] = ZIOCpsMonad[R,E]

transparent inline def asyncZIO[R,E](using CpsAsyncEffectMonad[[X]=>>ZIO[R,E,X]]): Async.InferAsyncArg[[X]=>>ZIO[R,E,X]] =
   new Async.InferAsyncArg[[X]=>>ZIO[R,E,X]]

transparent inline def asyncRIO[R]: Async.InferAsyncArg[[X]=>>RIO[R,X]] =
   new Async.InferAsyncArg[[X]=>>RIO[R,X]](using ZIOCpsMonad[R, Throwable])


given zioToZio[R1,R2<:R1,E1,E2](using ThrowableAdapter[R1,E1], ThrowableAdapter[R2,E2]): 
                                                         CpsMonadConversion[[T] =>> ZIO[R1,E1,T], [T]=>>ZIO[R2,E2,T]] with

    def apply[T](ft:ZIO[R1,E1,T]): ZIO[R2,E2,T]=
        val r1: ZIO[R2,E2,T] = ft.foldM(
          e1 => {
              val ex = summon[ThrowableAdapter[R1,E1]].toThrowable(e1)
              summon[ThrowableAdapter[R2,E2]].fromThrowable(ex)
          },
          v => ZIO.succeed(v)
        )
        val r2: ZIO[R2,E2,T] = r1
        r2

                                

given futureZIOConversion[R,E](using zio.Runtime[R], ThrowableAdapter[R,E]):
                                       CpsMonadConversion[[T]=>>ZIO[R,E,T],Future] with

   def apply[T](ft:ZIO[R,E,T]): Future[T]  =
        summon[Runtime[R]].unsafeRunToFuture(ft.mapError(e => summon[ThrowableAdapter[R,E]].toThrowable(e)))


given zioMemoization[R,E]: CpsMonadDynamicMemoization[[X]=>>ZIO[R,E,X]] with {}

 

given zioDynamicMemoizationAp[R1,E1,R2>:R1,E2<:E1,T]: CpsMonadDynamicMemoizationAp[[X]=>>ZIO[R1,E1,X],T,ZIO[R2,E2,T]] with      

   def apply(ft:ZIO[R2,E2,T]):ZIO[R1,E1,ZIO[R2,E2,T]] =
        ft.memoize


inline transparent given awaitValueDiscard[R,E](using CpsMonad[[X]=>>ZIO[R,E,X]]): ValueDiscard[ZIO[R,E,Unit]] = AwaitValueDiscard[[X]=>>ZIO[R,E,X],Unit]

