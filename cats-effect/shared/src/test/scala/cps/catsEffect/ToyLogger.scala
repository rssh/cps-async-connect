package cps.catsEffect

import cats.*
import cats.effect.*
import cps.*

import scala.annotation.experimental

class ToyLogger(ref: Ref[IO,Vector[String]]):

  def log(message: String):IO[Unit] =
    ref.update(lines => lines :+ message)

  def all(): IO[Vector[String]] =
    ref.get
  

object ToyLogger:

  def make(): IO[ToyLogger] =
    Ref.of[IO,Vector[String]](Vector.empty).map(ToyLogger(_))


@experimental
class DToyLogger(ref: Ref[IO,Vector[String]]):

  def log(message: String)(using CpsDirect[IO]): Unit =
    await(ref.update(lines => lines :+ message))


  def all()(using CpsDirect[IO]): Vector[String] =
    await(ref.get)
    
@experimental    
object DToyLogger:    
  
  def make()(using CpsDirect[IO]): DToyLogger =
    val ref = await(Ref.of[IO,Vector[String]](Vector.empty))
    DToyLogger(ref)
    