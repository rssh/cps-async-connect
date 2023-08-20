package cps.catsEffect

import cats.*
import cats.effect.*
import cps.*

import scala.annotation.experimental

@experimental
object directRefs {

  opaque type DirectRef[F[_],T] = Ref[F,T]

  object DirectRef {
    def apply[F[_],T](ref:Ref[IO,T]):DirectRef[IO,T] = ref
  }

  extension[F[_],A] (ref: DirectRef[F,A]) {

    def get(using CpsDirect[F]): A =
      await(ref.get)

    def set(using CpsDirect[F])(a: A): Unit =
      await(ref.set(a))

    def update(using CpsDirect[F])(f: A => A): Unit =
      await(ref.update(f))

  }

  extension (io: IO.type) {

    def directRefOf[T](t: T)(using cpsDirect: CpsDirect[IO]): DirectRef[IO, T] =
      DirectRef(await(Ref.of[IO, T](t)))

  }


}

