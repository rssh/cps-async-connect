package cps.monads.catsEffect
/*
 * (C) Ruslan Shevchenko <ruslan@shevchenko.kiev.ua>
 * 2021
 */

import cats.effect._
import cps._

import scala.util._
import scala.concurrent._

/**
 * CpsAsyncMonad for cats-effect.
 **/
class CatsIOCpsAsyncMonad extends CatsAsync[IO] with CpsAsyncEffectMonad[IO]:

  type F[T] = IO[T]


given catsIO: CatsIOCpsAsyncMonad = CatsIOCpsAsyncMonad()


given ioToFutureConversion(using runtime: unsafe.IORuntime): CpsMonadConversion[IO,Future] with

   def apply[T](io:IO[T]): Future[T] =
               io.unsafeToFuture() 


