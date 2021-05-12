package cps.monads.cats
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
class CatsIOCpsAsyncMonad extends CatsAsync[IO] with CpsAsyncMonad[IO]:

  type F[T] = IO[T]


given catsIO: CpsAsyncMonad[IO] = CatsIOCpsAsyncMonad()


given ioToFutureConversion[T](using runtime: unsafe.IORuntime): Conversion[IO[T],Future[T]] with

   def apply(io:IO[T]): Future[T] =
               io.unsafeToFuture() 


