package cps.monads.zio

import cps._
import zio._
import scala.util._
import scala.concurrent._

class URIOCpsMonad[R] extends CpsMonad[[X]=>>ZIO[R,Nothing,X]]:

  type F[T] = ZIO[R,Nothing,T]

  def pure[A](x:A): F[A] = URIO.succeed(x)

  def map[A,B](fa: F[A])(f: A=>B): F[B] =
      fa.map(f)

  def flatMap[A,B](fa: F[A])(f: A=> F[B]): F[B] =
      fa.flatMap(f)



object UIOCpsMonad extends URIOCpsMonad[Any]

given uioCpsMonad: CpsMonad[[X]=>>UIO[X]] = UIOCpsMonad

given urioCpsMonad[R] : CpsMonad[[X]=>>URIO[R,X]] = URIOCpsMonad[R]

given urioZioConversion[R1,R2<:R1,E]: CpsMonadConversion[[X]=>>ZIO[R1,Nothing,X],[X]=>>ZIO[R2,E,X]] with

   def apply[T](mf: CpsMonad[[X]=>>ZIO[R1,Nothing,X]], mg: CpsMonad[[X]=>>ZIO[R2,E,X]], uriot:URIO[R1,T]): ZIO[R2, E, T] =
           uriot


given urioRioConversion[R1,R2<:R1]: CpsMonadConversion[[X]=>>ZIO[R1,Nothing,X],[X]=>>RIO[R2,X]] with

   def apply[T](mf: CpsMonad[[X]=>>ZIO[R1,Nothing,X]], mg: CpsMonad[[X]=>>RIO[R2,X]], uriot:URIO[R1,T]): RIO[R2, T] =
           uriot



transparent inline def asyncURIO[R]: Async.InferAsyncArg[[X]=>>URIO[R,X]] =
   new Async.InferAsyncArg[[X]=>>URIO[R,X]](using URIOCpsMonad[R])


