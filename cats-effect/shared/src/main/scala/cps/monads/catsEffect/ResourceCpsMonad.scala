package cps.monads.catsEffect
/*
 * (C) Ruslan Shevchenko <ruslan@shevchenko.kiev.ua>
 * 2021
 */

import scala.util.{Try,Success,Failure}

import cats.*
import cats.effect.*
import cats.effect.kernel.*

import cps.*

/**
 * into F[T] to [A] =>> Resource[F,A] for using inside asyncScope
 **/
given resourceConversion[F[_]]: CpsMonadConversion[F, [A] =>> Resource[F,A]] = 
    new CpsMonadConversion[F, [A]=>>Resource[F,A]] {
        def apply[T](ft:F[T]):Resource[F,T] = Resource.eval(ft)
    }

/**
 * part of asyncScope
 *@see asyncScope
 */
class AsyncScopeInferArg[F[_]](using CpsTryMonad[[A]=>>Resource[F,A]], MonadCancel[F,Throwable]) {

    transparent inline def apply[T](inline body: T):F[T] =
            async[[X]=>>Resource[F,X]].apply(body).use(t=>summon[MonadCancel[F,Throwable]].pure(t))

}

/**
 * Produce effect with resource-aware scope block. 
 *
 * ```
 * val effect = asyncScope[IO] {
 *     val reader = await(openFile(input))
 *     val writer = await(openFile(output))
 *     writer.transformFrom(0,Long.MaxValue,reader)
 * }
 * ```
 * Here evaluation of effect will open reader and wrier, transfer data and then close reader and writer.
 * block inside asyncScope evaluated in CpsResourceMonad[[X]=>>Resource[F,X]]
 *@see [cps.monads.catsEffect.CpsResourceMonad]
 */
def asyncScope[F[_]](using CpsTryMonad[[A]=>>Resource[F,A]], MonadCancel[F,Throwable]) = AsyncScopeInferArg[F]()

given catsResourceMemoization[F[_]](using Concurrent[[X]=>>Resource[F,X]]):CpsMonadPureMemoization[[X]=>>Resource[F,X]] with

   def apply[T](ft: Resource[F,T]): Resource[F,Resource[F,T]] =
      summon[Concurrent[[X]=>>Resource[F,X]]].memoize(ft)
