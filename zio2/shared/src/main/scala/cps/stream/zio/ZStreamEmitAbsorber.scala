package cps.stream.zio

import zio.*
import zio.stream.*


import cps.{*,given}
import cps.monads.zio.{*,given}
import cps.stream.{*,given}
import scala.concurrent.*


given ZStreamEmitAbsorber[R,E,O](using ExecutionContext):  BaseUnfoldCpsAsyncEmitAbsorber[ZStream[R,E,O], [X]=>>ZIO[R,E,X], CpsMonadInstanceContextBody[[X]=>>ZIO[R,E,X]], O] with 

  override type Element = O

  override def asSync(fs:ZIO[R,E,ZStream[R,E,O]]):ZStream[R,E,O] =
        ZStream.unwrap(fs)

  def unfold[S](s0:S)(f:S => ZIO[R,E,Option[(O,S)]]): ZStream[R,E,O] =
        ZStream.unfoldZIO[R,E,O,S](s0)(f)

