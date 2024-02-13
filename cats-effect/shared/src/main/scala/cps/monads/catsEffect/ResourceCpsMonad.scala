package cps.monads.catsEffect
/*
 * (C) Ruslan Shevchenko <ruslan@shevchenko.kiev.ua>
 * 2021 - 2023
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
class AsyncScopeInferArg[F[_],C <: CpsMonadContext[[A]=>>Resource[F,A]]](using am: CpsTryMonad.Aux[[A]=>>Resource[F,A],C], mc: MonadCancel[F,Throwable]) {

    transparent inline def apply[T](inline body: C ?=> T):F[T] =
      am.apply(cps.macros.Async.transformContextLambda(body)).use(t=>summon[MonadCancel[F,Throwable]].pure(t))

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
def asyncScope[F[_]](using m:CpsTryMonad[[A]=>>Resource[F,A]], mc:MonadCancel[F,Throwable]) = AsyncScopeInferArg(using m, mc)

/**
 * Synonym for `asyncScope`.
 **/
def reifyScope[F[_]](using m:CpsTryMonad[[A]=>>Resource[F,A]], mc:MonadCancel[F,Throwable]) = AsyncScopeInferArg(using m, mc)

