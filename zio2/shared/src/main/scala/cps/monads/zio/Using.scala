package cps.monads.zio

import cps.*
import zio.*
import zio.managed.*

/**
 * pseudo-synchronious variant of `use` for using inside async block. 
 */
extension [R,E,A](r: ZManaged[R,E,A])(using m:CpsTryMonad[[X]=>>ZIO[R,E,X]])

    transparent inline def useOn[B](inline f: A=>B)(using CpsMonadContext[[X]=>>ZIO[R,E,X]]): B = 
        await(r.use(a => m.pure(f(a)) ))

/**
 * using ZManaged resource and close `r` after f will be finished.
 * Shpuld be used inside async block,  'f' can contains awaits.
 */
extension (zm: ZManaged.type)

    transparent inline def using[R,E,A,B](r:ZManaged[R,E,A])(inline f: A=>B)(using m:CpsTryMonad[[X]=>>ZIO[R,E,X]], mc:CpsMonadContext[[X]=>>ZIO[R,E,X]]): B =
        await(r.use(a => m.pure(f(a)) ))

    transparent inline def using[R, E, A1, A2, B](r1:ZManaged[R,E,A1], r2:ZManaged[R,E,A2])(inline f: (A1,A2)=>B)(using m:CpsTryMonad[[X]=>>ZIO[R,E,X]], mc:CpsMonadContext[[X]=>>ZIO[R,E,X]]): B =
        await(r1.use(a1 => r2.use(a2 => m.pure(f(a1,a2)))))
  
    transparent inline def using[R, E, A1, A2, A3, B](r1:ZManaged[R,E,A1], r2:ZManaged[R,E,A2], r3: ZManaged[R,E,A3])(inline f: (A1,A2,A3)=>B)(using m:CpsTryMonad[[X]=>>ZIO[R,E,X]], mc:CpsMonadContext[[X]=>>ZIO[R,E,X]]): B =
        await(r1.use(a1 => r2.use(a2 => r3.use(a3 => m.pure(f(a1,a2,a3))))))
  
