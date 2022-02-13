package cps.monads.zio

import cps._
import cps.macros._
import zio._
import scala.util._
import scala.concurrent._


/**
 * CpsMonad which encapsulate effects with automatic resource management.
 *
 * Example of usage:
 * ```
 *   asyncRManaged[R] {
 *       val input = FileChannel.open(inputPath)
 *       val output = FileChannel.open(outputPath)
 *       input.transformTo(0,Long.MaxValue,output)
 *   }
 * ```
 **/
class ZManagedCpsMonad[R, E] extends CpsTryMonad[[X]=>>ZManaged[R,E,X]] with CpsMonadInstanceContext[[X]=>>ZManaged[R,E,X]]:

  type F[T] = ZManaged[R,E,T]

  def pure[A](x:A):ZManaged[R,E,A] = ZManaged.succeed(x)

  def map[A,B](fa: F[A])(f: A=>B): F[B] =
      fa.map(f)

  def flatMap[A,B](fa: F[A])(f: A=> F[B]): F[B] =
      fa.flatMap(f)

  def error[A](e: Throwable): F[A] = 
      ZManaged.fromEffect(GenericThrowableAdapter.fromThrowable(e))

  def flatMapTry[A, B](fa: F[A])(f: util.Try[A] => F[B]): F[B] =
      fa.foldM(
          e => f(Failure(GenericThrowableAdapter.toThrowable(e))),
          a => f(Success(a))
      )
           
       


object TaskManagedCpsMonad extends ZManagedCpsMonad[Any,Throwable]

given CpsTryMonad[TaskManaged] = TaskManagedCpsMonad


given zManagedCpsMonad[R,E]: ZManagedCpsMonad[R,E] = ZManagedCpsMonad[R,E]

transparent inline def asyncZManaged[R,E](using CpsTryMonad[[X]=>>ZManaged[R,E,X]]): Async.InferAsyncArg[[X]=>>ZManaged[R,E,X],CpsMonadInstanceContextBody[[X]=>>ZManaged[R,E,X]]] =
   new cps.macros.Async.InferAsyncArg

transparent inline def asyncRManaged[R]: Async.InferAsyncArg[[X]=>>RManaged[R,X],CpsMonadInstanceContextBody[[X]=>>ZManaged[R,Throwable,X]]] =
   new Async.InferAsyncArg(using ZManagedCpsMonad[R, Throwable])


given zioToZManaged[R1,R2<:R1,E1,E2>:E1]: 
                                          CpsMonadConversion[[T] =>> ZIO[R1,E1,T], 
                                                             [T]=>> ZManaged[R2,E2,T]] with

    def apply[T](ft:ZIO[R1,E1,T]): ZManaged[R2,E2,T]=
        ZManaged.fromEffect[R2,E2,T](ft)

                                

