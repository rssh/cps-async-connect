package cps.monads.catsEffect
/*
 * (C) Ruslan Shevchenko <ruslan@shevchenko.kiev.ua>
 * 2021
 */

import cats.effect.*
import cats.effect.kernel.*

import cps.*


 /***
 * Pseudo-synchronious syntax for resource, which can be used in async block.
 *
 * Usage:
 *  assuming we have:
 * ```
 *     def open(file: File): Resource[IO, BufferedReader]
 * ```
 * we can 
 * ```
 *     async[IO] {
 *        ....  
 *        open(file).useOn{ buffer =>
 *            await(doSomething)
 *            buffer.write(r)
 *            result
 *        }
 *     }
 * ```
 **/
 extension [F[_],A](r: Resource[F,A])(using m:CpsMonad[F], cm: MonadCancel[F,Throwable]) 

    //  bug or undefined specs in dotty: we can't name extension if we have method with the same name
    transparent inline def useOn[B](inline f: A=>B): B = 
        await(r.use(a => m.pure(f(a)) ))


/***
 * Pseudo-synchronious syntax for resource, which can be used in async block.
 *
 * ```
 *     async[IO] {
 *        ....  
 *        useing(openFile){ buffer =>
 *            await(doSomething)
 *            buffer.write(r)
 *            result
 *        }
 *     }
 * ```
 **/
transparent inline def using[F[_],A, B](r:Resource[F,A])(inline f: A=>B)(using m:CpsMonad[F], cm: MonadCancel[F,Throwable]): B =
     await(r.use(a => m.pure(f(a)) ))

