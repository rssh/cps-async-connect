package cps.monads.scalaz

import cps._
import scalaz._
import scalaz.effect._

import scala.util.Try

given scalazIO: CpsTryMonad[IO] with

   type F[T] = IO[T]

   def pure[A](a:A): IO[A] = IO(a)

   def map[A,B](fa: IO[A])(f: A=>B): IO[B] =
     fa.map(f)

   def flatMap[A,B](fa: IO[A])(f: A=>IO[B]): IO[B] =
     fa.flatMap(f)

   def error[A](e: Throwable): IO[A] =
     IO.throwIO[A](e)

   def flatMapTry[A,B](fa: IO[A])(f: Try[A]=>IO[B]) =
     fa.catchLeft.flatMap{ 
         _.fold( e => f(scala.util.Failure(e)), a => f(scala.util.Success(a)) )
     }

     

