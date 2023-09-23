package cps.monads.catsEffect

import cps.*
import cats.effect.*
import cats.effect.std.Dispatcher
import scala.concurrent.blocking
import java.util.concurrent.CompletableFuture

class CpsCERuntimeAwait[F[_]](dispatcher: Dispatcher[F], async: Async[F]) extends CpsRuntimeAwait[F] {


    override def runAsync[A, C <: CpsTryMonadContext[F]](f: C => A)(m: CpsAsyncEffectMonad[F], ctx: C): F[A] = {
        async.async_[A] { cb =>
           Thread.startVirtualThread{ () =>
             val r = try {
               val a = f(ctx)
               cb(Right(a))
             } catch {
               case ex: Throwable =>
                 cb(Left(ex))
             }
           }
        }
    }


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

  override def apply[A](op: CpsRuntimeAwait[F] => A)(using CpsTryMonadContext[F], CpsAsyncEffectMonad[F]): F[A] = 
    Dispatcher.sequential[F].use(dispatcher => summon[Async[F]].delay(op(CpsCERuntimeAwait(dispatcher,summon[Async[F]]))))

  override def applyAsync[A](op: CpsRuntimeAwait[F] => F[A])(using CpsTryMonadContext[F], CpsAsyncEffectMonad[F]): F[A] = 
    Dispatcher.sequential[F].use(dispatcher => op(CpsCERuntimeAwait(dispatcher,summon[Async[F]])))

}

given cpsCERuntimeAwaitProvider[F[_]](using Async[F]): CpsRuntimeAwaitProvider[F] =
         CpsCERuntimeAwaitProvider[F]
