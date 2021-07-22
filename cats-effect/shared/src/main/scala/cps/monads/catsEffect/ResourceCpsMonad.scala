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
 * Monadic operations over F[] with brackets,  can be used as main monad in case of deeple nested resources,
 * to avoid nesting of scopes.
 *
 *@see asyncScope
 **/
class ResourceCpsMonad[F[_]](using cm: MonadCancel[F, Throwable]) extends CpsTryMonad[[A]=>>Resource[F,A]]:

   def pure[A](a:A): Resource[F,A] =
      Resource.pure[F,A](a)

   def map[A, B](fa: Resource[F,A])(f: A => B): Resource[F,B] = 
      fa.map(f)

   def flatMap[A, B](fa: Resource[F,A])(f: A => Resource[F,B]): Resource[F,B] = 
      fa.flatMap(f)

   def error[A](e: Throwable): Resource[F,A] = 
      Resource.eval(summon[MonadThrow[F]].raiseError[A](e))

   def flatMapTry[A, B](fa: Resource[F,A])(f: Try[A] => Resource[F,B]): Resource[F,B] = 
      fa match
        case Resource.Allocate(ra) =>
          Resource.Bind(
            Resource.Allocate[F,Try[A]](fk => summon[MonadThrow[F]].redeem(ra(fk))(
                                       ex => (Failure(ex), (e:Resource.ExitCase) => cm.pure(()) ),
                                       (a,fin)  => (Success(a), fin)
                                    )),
            f
          )
        case Resource.Bind(source, prevF) =>
            flatMapTry(source){
               case Failure(ex) => f(Failure(ex))
               case Success(pa) => flatMapTry(prevF(pa)){
                                      case Failure(ex) => f(Failure(ex))
                                      case Success(a) => f(Success(a))
                                   }
            }
        case Resource.Eval(fa) =>
                Resource.Bind(Resource.Eval(summon[MonadThrow[F]].redeem(fa)( ex => Failure(ex), a => Success(a) )), f)
        case Resource.Pure(a) =>
                Resource.Bind(Resource.Pure(Success(a)), f)

      


given resourceCpsMonad[F[_]](using cm: MonadCancel[F, Throwable]): CpsTryMonad[[A]=>>Resource[F,A]] =
       ResourceCpsMonad[F]


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

