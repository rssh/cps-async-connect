package cps.monads.catsEffect

import cps._
import cats.{Monad, MonadThrow}
import cats.effect.kernel._

import scala.util._
import scala.util.control.NonFatal
import scala.concurrent._

class CatsMonad[F[_]](using mf: Monad[F]) extends CpsMonad[F] with CpsMonadInstanceContext[F]:

  def pure[A](a:A): F[A] =
    mf.pure(a)

  def map[A,B](fa: F[A])(f: A => B): F[B] =
    mf.map(fa)(f)

  def flatMap[A,B](fa: F[A])(f: A => F[B]): F[B] =
    mf.flatMap(fa)(f)


class CatsMonadThrow[F[_]](using MonadThrow[F]) extends CatsMonad[F] with CpsTryMonad[F]:

  def error[A](e: Throwable): F[A] =
    summon[MonadThrow[F]].raiseError(e)

  override def mapTry[A,B](fa:F[A])(f: Try[A]=>B): F[B] =
    summon[MonadThrow[F]].redeem(fa)( ex => f(Failure(ex)), a => f(Success(a)) )

  def flatMapTry[A,B](fa:F[A])(f: Try[A]=>F[B]): F[B] =
    summon[MonadThrow[F]].redeemWith(fa)( ex => f(Failure(ex)), a => f(Success(a)) )


class CatsAsync[F[_]](using Async[F]) extends CatsMonadThrow[F] with CpsAsyncEffectMonad[F]:

  def adoptCallbackStyle[A](source: (Try[A]=>Unit) => Unit): F[A] =
    def adoptIOCallback(ioCallback: Either[Throwable, A]=>Unit): Try[A]=>Unit = {
       case Failure(ex) => ioCallback(Left(ex))
       case Success(a) => ioCallback(Right(a))
    }
    summon[Async[F]].async_ {
      ioCallback => source(adoptIOCallback(ioCallback))
    }


class CatsConcurrent[F[_]](using Concurrent[F], Async[F]) extends CatsAsync[F] with CpsConcurrentEffectMonad[F]:

  type Spawned[A] = Fiber[F,Throwable,A]

  def spawnEffect[A](op: => F[A]): F[Spawned[A]] = 
    summon[Concurrent[F]].start{
      try 
        op
      catch
        case NonFatal(ex) =>
          error(ex)
    }

  def join[A](op: Spawned[A]): F[A] =
     flatMap(op.join){ r =>
       r match
         case Outcome.Succeeded(fa) => fa
         case Outcome.Errored(ex) => error(ex)
         case Outcome.Canceled() => error(new CancellationException("fiber is cancelled"))
     }

  def tryCancel[A](op: Spawned[A]): F[Unit] =
    op.cancel




given catsMonad[F[_]](using Monad[F]): CpsMonad[F] = CatsMonad[F]()

given catsMonadThrow[F[_]](using MonadThrow[F]): CpsTryMonad[F] = CatsMonadThrow[F]()

given catsAsync[F[_]](using Async[F], NotGiven[Concurrent[F]]): CpsAsyncEffectMonad[F] = CatsAsync[F]()

given catsConcurrent[F[_]](using Concurrent[F], Async[F]): CpsConcurrentEffectMonad[F] = CatsConcurrent[F]()

given catsMemoization[F[_]](using Concurrent[F]) :CpsMonadMemoization.Pure[F] with
    
  def apply[T](ft:F[T]): F[F[T]] =
    summon[Concurrent[F]].memoize(ft)

inline transparent given catsUnitValueDiscard[F[_]](using CpsAsyncMonad[F]): ValueDiscard[F[Unit]] = AwaitValueDiscard[F,Unit]
inline transparent given catsIntValueDiscard[F[_]](using CpsAsyncMonad[F]): ValueDiscard[F[Int]] = AwaitValueDiscard[F,Int]


