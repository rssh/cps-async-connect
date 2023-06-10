package cps.monads.zio

import cps._
import cps.macros._
import zio._
import scala.util._
import scala.concurrent._



class ZIOCpsMonad[R, E] extends CpsConcurrentEffectMonad[[X]=>>ZIO[R,E,X]] with CpsMonadInstanceContext[[X]=>>ZIO[R,E,X]]:

  type F[T] = ZIO[R,E,T]

  type Spawned[T] = Fiber[E,T]

  def pure[A](x:A):ZIO[R,E,A] = ZIO.succeed(x)

  def map[A,B](fa: F[A])(f: A=>B): F[B] =
      fa.map(f)

  def flatMap[A,B](fa: F[A])(f: A=> F[B]): F[B] =
      fa.flatMap(f)

  def error[A](e: Throwable): F[A] = 
      e match
        case ZIOErrorAdapter(e1) => ZIO.fail(e1.asInstanceOf[E])
        case other => ZIO.fail(other.asInstanceOf[E])

  def flatMapTry[A, B](fa: F[A])(f: util.Try[A] => F[B]): F[B] =
      fa.foldM(
          e => f(Failure(GenericThrowableAdapter.toThrowable(e))),
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

  def spawnEffect[A](op: =>F[A]):ZIO[R,E,Spawned[A]] =
      op.fork

  def join[A](op: Fiber[E,A]) =
      op.join

  def tryCancel[A](op: Fiber[E,A]):F[Unit] =
      op.interrupt.map(_ => ()) 



//object TaskCpsMonad extends ZIOCpsMonad[Any,Throwable]
//
//given CpsConcurrentEffectMonad[Task] = TaskCpsMonad

given zioCpsMonad[R,E]: ZIOCpsMonad[R,E] = ZIOCpsMonad[R,E]

transparent inline def asyncZIO[R,E](using CpsConcurrentEffectMonad[[X]=>>ZIO[R,E,X]]): Async.InferAsyncArg[[X]=>>ZIO[R,E,X],CpsMonadInstanceContextBody[[X]=>>ZIO[R,E,X]]] =
   new cps.macros.Async.InferAsyncArg

transparent inline def asyncRIO[R]: Async.InferAsyncArg[[X]=>>RIO[R,X], CpsMonadInstanceContextBody[[X]=>>ZIO[R,Throwable,X]]] =
   new Async.InferAsyncArg(using ZIOCpsMonad[R, Throwable])


given zioToZio[R1,R2<:R1,E1,E2>:E1]: CpsMonadConversion[[T] =>> ZIO[R1,E1,T], [T]=>>ZIO[R2,E2,T]] with
                               
    def apply[T](ft:ZIO[R1,E1,T]): ZIO[R2,E2,T]= ft


//given zioToRio[R]: CpsMonadConversion[[T] =>> ZIO[Nothing,Any,T], [T]=>>RIO[R,T]] with
//
//    def apply[T](ft:ZIO[Nothing,Any,T]): RIO[R,T] =
//        val r1 = ft.foldM(
//          e => {
//              ZIO.fail[Throwable](GenericThrowableAdapter.toThrowable(e))
//          },
//          v => ZIO.succeed(v)
//        )
//        val r2: RIO[R,T] = r1.asInstanceOf[ZIO[R,Throwable,T]]
//        r2

                                

given futureZIOConversion[R,E](using zio.Runtime[R]):
                                       CpsMonadConversion[[T]=>>ZIO[R,E,T],Future] with

   def apply[T](ft:ZIO[R,E,T]): Future[T]  =
        summon[Runtime[R]].unsafeRunToFuture(ft.mapError(e => GenericThrowableAdapter.toThrowable(e)))


given zioMemoization[R,E]: CpsMonadMemoization.Dynamic[[X]=>>ZIO[R,E,X]] with {}

 

given zioDynamicMemoizationAp[R1,E1,R2>:R1,E2<:E1,T]: CpsMonadMemoization.DynamicAp[[X]=>>ZIO[R1,E1,X],T,ZIO[R2,E2,T]] with      

   def apply(ft:ZIO[R2,E2,T]):ZIO[R1,E1,ZIO[R2,E2,T]] =
        ft.memoize


inline transparent given awaitValueDiscard[R,E](using CpsMonad[[X]=>>ZIO[R,E,X]]): ValueDiscard[ZIO[R,E,Unit]] = AwaitValueDiscard[[X]=>>ZIO[R,E,X],Unit]

