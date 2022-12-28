package cps.monads.probability

import probability_monad.*
import cps.*

import scala.util.Try

given DistributionCpsMonad: CpsTryMonad[Distribution] with CpsMonadInstanceContext[Distribution] with {

  def pure[A](a:A): Distribution[A] = 
    Distribution.always(a)

  def map[A,B](fa:Distribution[A])(f:A=>B):Distribution[B] =
    fa.map(f)

  def flatMap[A,B](fa:Distribution[A])(f:A=>Distribution[B]):Distribution[B] =
    fa.flatMap(f)

  def error[A](e:Throwable): Distribution[A] = 
    new Distribution[A] {
       override def get = { throw e }
    }

  override def mapTry[A,B](fa:Distribution[A])(f:Try[A]=>B): Distribution[B] = 
    new MapTryDistribution(fa,f)

  def flatMapTry[A,B](fa:Distribution[A])(f:Try[A]=>Distribution[B]): Distribution[B] =
    new FlatMapTryDistribution(fa,f)

}

