package cps.monads.cats

import cps._
import cats.effect.kernel._

import scala.util._
import scala.concurrent._

class CatsSync[F[_]](using Sync[F]) extends CpsTryMonad[F]:

  def pure[A](a:A): F[A] =
        summon[Sync[F]].pure(a)

  def map[A,B](fa: F[A])(f: A=>B): F[B] =
        summon[Sync[F]].map(fa)(f)

  def flatMap[A,B](fa: F[A])(f: A=>F[B]): F[B] =
        summon[Sync[F]].flatMap(fa)(f)

  def error[A](e: Throwable): F[A] =
        summon[Sync[F]].raiseError(e)

  override def mapTry[A,B](fa:F[A])(f: Try[A]=>B): F[B] =
        summon[Sync[F]].redeem(fa)( ex => f(Failure(ex)), a => f(Success(a)) )

  def flatMapTry[A,B](fa:F[A])(f: Try[A]=>F[B]): F[B] =
        summon[Sync[F]].redeemWith(fa)( ex => f(Failure(ex)), a => f(Success(a)) )


class CatsAsync[F[_]](using Async[F]) extends CatsSync[F] with CpsAsyncMonad[F]:


  def adoptCallbackStyle[A](source: (Try[A]=>Unit) => Unit): F[A] =
        def adoptIOCallback(ioCallback: Either[Throwable, A]=>Unit): Try[A]=>Unit = {
           case Failure(ex) => ioCallback(Left(ex))
           case Success(a) => ioCallback(Right(a))
        }
        summon[Async[F]].async_ {
          ioCallback => source(adoptIOCallback(ioCallback))
        }


given catsSync[F[_]](using Sync[F]): CpsTryMonad[F] = CatsSync[F]()

given catsAsync[F[_]](using Async[F]): CpsAsyncMonad[F] = CatsAsync[F]()



