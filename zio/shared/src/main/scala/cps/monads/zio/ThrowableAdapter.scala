package cps.monads.zio

import zio._

trait ThrowableAdapter[-R,E]:

   def toThrowable(e:E): Throwable 

   def fromThrowable[A](e:Throwable): ZIO[R,E,A]



// TODO:  rething, mb change to static check
given throwableForThrowable[R, X <: Throwable]: ThrowableAdapter[R, X] with
   def toThrowable(e: X): Throwable = e

   def fromThrowable[A](e: Throwable): ZIO[R, X, A] = 
          ZIO.fail(e.asInstanceOf[X])

