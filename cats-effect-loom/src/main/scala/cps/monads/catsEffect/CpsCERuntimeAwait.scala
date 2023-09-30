package cps.monads.catsEffect

import cps.*
import cats.effect.*
import cats.effect.std.Dispatcher

import scala.concurrent.blocking
import java.util.concurrent.CompletableFuture
import scala.util.control.NonFatal

class CpsCERuntimeAwait[F[_]](dispatcher: Dispatcher[F], async: Async[F]) extends CpsRuntimeAwait[F] {

    override def await[A](fa: F[A])(ctx: CpsTryMonadContext[F]): A = {
         import scala.concurrent.ExecutionContext.Implicits.global
         val cf = CompletableFuture[A]()
         dispatcher.unsafeToFuture(fa).onComplete{
            case scala.util.Success(a) => cf.complete(a)
            case scala.util.Failure(ex) => cf.completeExceptionally(ex)
         }
         blocking {
           cf.get()
         }
    }

}

class CpsCERuntimeAwaitProvider[F[_]:Async] extends CpsRuntimeAwaitProvider[F] {

  def runInVirtualThread[A](op: =>A): F[A] =
    Async[F].async_{ cb =>
      Thread.ofVirtual().start(() => {
        try {
          val r = op
          cb(Right(r))
        }catch{
          case NonFatal(ex) =>
            cb(Left(ex))
        }
      })
    }

  override def withRuntimeAwait[A](op: CpsRuntimeAwait[F] => F[A])(using ctx:CpsTryMonadContext[F]): F[A] =
    Dispatcher.sequential[F].use{dispatcher =>
       val ra = CpsCERuntimeAwait(dispatcher, summon[Async[F]])
       ctx.monad.flatten(runInVirtualThread(op(ra)))
    }

}

given cpsCERuntimeAwaitProvider[F[_]](using Async[F]): CpsRuntimeAwaitProvider[F] =
         CpsCERuntimeAwaitProvider[F]
