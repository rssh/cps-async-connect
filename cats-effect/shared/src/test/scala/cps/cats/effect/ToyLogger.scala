package cps.cats.effect

import cats._
import cats.effect._

class ToyLogger(ref: Ref[IO,Vector[String]]):

  def log(message: String):IO[Unit] =
    ref.update(lines => lines :+ message)

  def all(): IO[Vector[String]] =
    ref.get
  

object ToyLogger:

  def make(): IO[ToyLogger] =
    Ref.of[IO,Vector[String]](Vector.empty).map(ToyLogger(_))

