package cpszio


import zio.{given, *}
import cps.monads.zio.{given, *}
import cps.{given, *}

object Issue4Example:
  private val ref = zio.FiberRef.make(Map.empty[String, Any])

  def withCtx[R](body: => Task[R]) = async[[T]=>>ZIO[Scope,Throwable,T]] {
    val ctx = ref
    body
  }

  //def withCtxNoAwait[R](body:  => Task[R]):Task[Unit] = { 
  //
  //  ref1.map(_ => ())
  //}

  