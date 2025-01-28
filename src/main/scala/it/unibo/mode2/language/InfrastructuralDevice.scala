package it.unibo.mode2.language

sealed trait Device[ID: Ordering]:
  type Capabilities
trait Application[ID] extends Device[ID]
trait Infrastructural[ID] extends Device[ID]

class Placement[C <: Component[?, ?], D <: Device[?]](val component: C, val device: D)(using
    device.Capabilities <:< component.Capabilities
)

class Deployment[A <: Application[?], C <: Component[?, ?], D <: Device[?]](
    val applicationDevice: A,
    val placement: Placement[C, D]
)

object InfrastructuralDsl:
  extension [C <: Component[?, ?], D <: Device[?]](component: C)
    infix def deployedOn(device: D)(using device.Capabilities <:< component.Capabilities): Placement[C, D] =
      Placement(component, device)

  extension [A <: Application[?], C <: Component[?, ?], D <: Device[?]](placement: Placement[C, D])
    infix def forDevice(device: A): Deployment[A, C, D] = Deployment(device, placement)
