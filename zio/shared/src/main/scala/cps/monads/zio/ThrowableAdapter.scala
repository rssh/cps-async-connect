package cps.monads.zio

import zio._


case class ZIOErrorAdapter[E](e:E) extends RuntimeException

object GenericThrowableAdapter:

   def toThrowable[E](e:E): Throwable =
      e match
         case eth: Throwable => eth
         case _  => new ZIOErrorAdapter(e)

   def fromThrowable[R,E,A](e:Throwable): ZIO[R,E,A] =
      e match
         case ZIOErrorAdapter(e1) => ZIO.fail(e1.asInstanceOf[E])
         case other => ZIO.fail( other.asInstanceOf[E] )


/*
// TODO:  rething, mb change to static check
given throwableForThrowable[R, X <: Throwable]: ThrowableAdapter[R, X] with
   def toThrowable(e: X): Throwable = e

   def fromThrowable[A](e: Throwable): ZIO[R, X, A] = 
          ZIO.fail(e.asInstanceOf[X])
*/
