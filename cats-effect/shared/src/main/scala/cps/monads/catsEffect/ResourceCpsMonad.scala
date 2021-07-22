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
 * async[[X]=>>Resource[IO,X]] {
 *     val reader = await(openFile(input))
 *     val writer = await(openFile(output))
 *     writer.writeAll(reader)
 * }
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



