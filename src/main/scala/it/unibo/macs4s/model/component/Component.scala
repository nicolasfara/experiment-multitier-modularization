package it.unibo.macs4s.model.component

import it.unibo.macs4s.model.CollectiveData
import it.unibo.macs4s.model.scheduling.SchedulingPolicy
import it.unibo.macs4s.platform.Context

sealed trait Component[-Input <: Product, +Output]:
  self: SchedulingPolicy =>
  type Capabilities
  final def withCapability[Out](f: PartialFunction[Capabilities, Out])(using Context): Out = ???

trait LocalComponent[-Input <: Product, +Output] extends Component[Input, Output]:
  self: SchedulingPolicy =>

  def apply(input: Input): Context ?=> Output

trait CollectiveComponent[-Input <: Product, +Output] extends Component[Input, Output]:
  self: SchedulingPolicy =>

  final def sharedData(using context: Context): CollectiveData[context.DeviceId, Output] =
    context.collectiveDataFor(this)

  def apply(input: Input): Context ?=> Output
