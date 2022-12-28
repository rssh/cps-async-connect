package cps.monads.probability

import probability_monad.*
import cps.*

given DistributionCpsMonad: CpsMonad[Distribution] with CpsMonadInstanceContext[Distribution] with {

  def pure[A](a:A): Distribution[A] = 
    Distribution.always(a)

  def map[A,B](fa:Distribution[A])(f:A=>B):Distribution[B] =
    fa.map(f)

  def flatMap[A,B](fa:Distribution[A])(f:A=>Distribution[B]):Distribution[B] =
    fa.flatMap(f)

}

