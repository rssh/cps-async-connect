package cps.monads.zio

import _root_.zio.*
import cps.*
import cps.monads.zio.{*, given}

import scala.concurrent.*
import java.util.concurrent.CompletableFuture
import scala.util.control.NonFatal


class ZIOCpsRuntimeAwait[R,E](runtime: Runtime[R])(implicit trace:Trace) extends CpsRuntimeAwait[[X] =>> ZIO[R,E,X]] {


  override def await[A](fa: ZIO[R,E,A])(ctx: CpsTryMonadContext[[X]=>>ZIO[R,E,X]]): A =
    val cf = new CompletableFuture[Exit[E,A]]()
    //  runtime is not a clear analog of Dispatcher, but
    Unsafe.unsafely {
      runtime.unsafe.runOrFork(fa) match
        case Left(fiberRuntime) =>
          fiberRuntime.unsafe.addObserver((v:Exit[E,A]) => cf.complete(v))
        case Right(v) => cf.complete(v)
    }
    blocking {
      val cfres = try {
          cf.get()
        } catch
          case ex: ExecutionException =>
            throw ex.getCause()
      cfres match
        case Exit.Success(a) =>
          a
        case Exit.Failure(cause) =>
          if (cause.isEmpty) {
            throw new ZIOErrorAdapter[String]("ZIO fiber failed with empty list of failures")
          } else {
            val e = cause.failures.head
            val te = if (e.isInstanceOf[Throwable]) then {
                        e.asInstanceOf[Throwable]
                      } else {
                        new ZIOErrorAdapter[E](e)
                      }
            for(se <- cause.failures.tail) {
                val tse = if (se.isInstanceOf[Throwable]) then {
                            se.asInstanceOf[Throwable]
                          } else {
                            new ZIOErrorAdapter[E](se)
                          }
                te.addSuppressed(tse)
            }
            throw te
          }

    }

}


class ZIORuntimeAwaitProvider[R,E >: Throwable,A](implicit trace: Trace) extends CpsRuntimeAwaitProvider[[A] =>> ZIO[R,E,A]]{


  def inVirtualThread[A](op: => ZIO[R,E,A]): ZIO[R,E,A] =
    ZIO.asyncZIO[R,E,A]{ (cb) =>
      val thread = Thread.startVirtualThread {
        () =>
          try {
            val r = op
            val _ = cb(r)
          } catch {
            case NonFatal(ex) =>
              val _ = cb(ZIO.fail(ex))
          }
      }
      ZIO.succeed(thread.threadId())
    }

  override def withRuntimeAwait[A](lambda: CpsRuntimeAwait[[X] =>> ZIO[R, E, X]] => ZIO[R,E,A])(using ctx: CpsTryMonadContext[[X]=>>ZIO[R,E,X]]): ZIO[R,E,A] = {
    ZIO.runtime[R].flatMap{ runtime =>
      val runtimeAwait = new ZIOCpsRuntimeAwait[R,E](runtime)
      // run in virtual thread for case, when passed lambda is invoked in the main flow of the HO function.
      // In such case, start of virtual thread will be faster then determination of blocking in the thread pool
      inVirtualThread(lambda(runtimeAwait))
    }
  }


}

given zioRuntimeAwaitProvider[R,E >: Throwable,A](using trace: Trace): CpsRuntimeAwaitProvider[[A] =>> ZIO[R,E,A]] =
   ZIORuntimeAwaitProvider[R,E,A]()