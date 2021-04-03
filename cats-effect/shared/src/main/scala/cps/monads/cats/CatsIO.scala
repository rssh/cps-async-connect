package cps.monads.cats

import cats.effect._
import cps._

import scala.util._
import scala.concurrent._


class CatsIOCpsAsyncMonad extends CpsAsyncMonad[IO]:

  type F[T] = IO[T]

  def pure[A](a:A): IO[A] = IO.pure(a)

  def map[A,B](fa:IO[A])(f: A=>B): IO[B] = 
        fa.map(f)

  def flatMap[A,B](fa:IO[A])(f: A=>IO[B]): IO[B] =
        fa.flatMap(f)

  def error[A](e: Throwable): IO[A] =
        IO.raiseError[A](e)

  def flatMapTry[A,B](fa: IO[A])(f: Try[A] => IO[B]): IO[B] =
        fa.redeemWith( ex => f(Failure(ex)), a => f(Success(a)) )

  override def mapTry[A,B](fa:IO[A])(f: Try[A]=>B): IO[B] =
        fa.redeem(ex => f(Failure(ex)), a => f(Success(a)) )


  def adoptCallbackStyle[A](source: (Try[A]=>Unit) => Unit): IO[A] =
        def adoptIOCallback(ioCallback: Either[Throwable, A]=>Unit): Try[A]=>Unit = {
           case Failure(ex) => ioCallback(Left(ex))
           case Success(a) => ioCallback(Right(a))
        }
        IO.async_ {
          ioCallback => source(adoptIOCallback(ioCallback))
        }



class CatsIOCpsSchedulingMonad(ec: ExecutionContext) extends CatsIOCpsAsyncMonad with CpsSchedulingMonad[IO]:

  def spawn[A](op: => IO[A]): IO[A] =
    IO.delay(op).flatten.evalOn(ec)



given catsIO: CpsAsyncMonad[IO] = CatsIOCpsAsyncMonad()

given catsIOScheduling(using ec: ExecutionContext): CpsSchedulingMonad[IO] =
        CatsIOCpsSchedulingMonad(ec) 

