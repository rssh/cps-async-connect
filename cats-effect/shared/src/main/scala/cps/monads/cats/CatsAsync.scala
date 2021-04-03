package cps.monads.cats

import cps._
import cats.effect.kernel._

import scala.util._

given catsAsync[F[_]](using Async[F]): CpsAsyncMonad[F] with

  def pure[A](a:A): F[A] =
        summon[Async[F]].pure(a)

  def map[A,B](fa: F[A])(f: A=>B): F[B] =
        summon[Async[F]].map(fa)(f)

  def flatMap[A,B](fa: F[A])(f: A=>F[B]): F[B] =
        summon[Async[F]].flatMap(fa)(f)

  def error[A](e: Throwable): F[A] =
        summon[Async[F]].raiseError(e)

  override def mapTry[A,B](fa:F[A])(f: Try[A]=>B): F[B] =
        summon[Async[F]].redeem(fa)( ex => f(Failure(ex)), a => f(Success(a)) )

  def flatMapTry[A,B](fa:F[A])(f: Try[A]=>F[B]): F[B] =
        summon[Async[F]].redeemWith(fa)( ex => f(Failure(ex)), a => f(Success(a)) )

  def adoptCallbackStyle[A](source: (Try[A]=>Unit) => Unit): F[A] =
        def adoptIOCallback(ioCallback: Either[Throwable, A]=>Unit): Try[A]=>Unit = {
           case Failure(ex) => ioCallback(Left(ex))
           case Success(a) => ioCallback(Right(a))
        }
        summon[Async[F]].async_ {
          ioCallback => source(adoptIOCallback(ioCallback))
        }


