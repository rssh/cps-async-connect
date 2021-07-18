package cps.monads.zio

import cps.*
import zio.*

/**
 * pseudo-synchronious variant of `use` for using inside async block. 
 */
extension [R,E,A](r: ZManaged[R,E,A])(using m:CpsTryMonad[[X]=>>ZIO[R,E,X]])

    transparent inline def useOn[B](inline f: A=>B): B = 
        await(r.use(a => m.pure(f(a)) ))

/**
 * using ZManaged resource and close `r` after f will be finished.
 * Shpuld be used inside async block,  'f' can contains awaits.
 */
transparent inline def using[R,E,A,B](r:ZManaged[R,E,A])(inline f: A=>B)(using m:CpsTryMonad[[X]=>>ZIO[R,E,X]]): B =
        await(r.use(a => m.pure(f(a)) ))


