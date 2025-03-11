package it.unibo.macs4s.language.deployment

import it.unibo.macs4s.model.component.Component

sealed trait Device:
  type Capabilities
  type Tie
trait Application extends Device
trait Infrastructural extends Device

class Placement[C <: Component[?, ?], D <: Device](val component: C, val device: D)(using
    device.Capabilities <:< component.Capabilities
)

class Deployment[A <: Application, C <: Component[?, ?], D <: Device](
    val applicationDevice: A,
    val placement: Placement[C, D]
)

object InfrastructuralDsl:
  class MacroDeploymentScope
  def deployment(init: MacroDeploymentScope ?=> Unit): Unit = ???

  class DeviceDeploymentScope
  def forDevice[A <: Application](device: A)(init: DeviceDeploymentScope ?=> Unit)(using MacroDeploymentScope): Unit =
    given t: DeviceDeploymentScope = new DeviceDeploymentScope

  extension [C <: Component[?, ?], D <: Device](component: C)
    infix def deployedOn(
        device: D
    )(using device.Capabilities <:< component.Capabilities, DeviceDeploymentScope): Placement[C, D] =
      Placement(component, device)
