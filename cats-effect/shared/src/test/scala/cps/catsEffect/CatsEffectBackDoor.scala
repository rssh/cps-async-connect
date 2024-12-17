package cats.effect

import cats.effect.unsafe.IORuntime

import scala.concurrent.ExecutionContext

object CatsEffectBackDoor {

  def blocking(ioRuntime: IORuntime): ExecutionContext = ioRuntime.blocking
  
}
