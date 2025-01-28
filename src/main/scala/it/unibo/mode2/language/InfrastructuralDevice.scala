package it.unibo.mode2.language

import it.unibo.mode2.{Bar, Foo, MovementDetection}

sealed trait Device[ID: Ordering]:
  type Capabilities
trait Application[ID] extends Device[ID]
trait Infrastructural[ID] extends Device[ID]

trait Smartphone[ID] extends Application[ID]:
  override type Capabilities = Bar
trait Wearable[ID] extends Infrastructural[ID]:
  override type Capabilities = Foo

object ApplicationSmartphone extends Smartphone[1]
object Smartphone2 extends Smartphone[2]
object InfrastructuralWearable extends Wearable[1]

class Placement[C <: Component[?, ?], D <: Device[?]](val component: C, val device: D)(using
    device.Capabilities <:< component.Capabilities
)

class Pippo[A <: Application[?], C <: Component[?, ?], D <: Device[?]](
    val applicationDevice: A,
    val placement: Placement[C, D]
)

extension [C <: Component[?, ?], D <: Device[?]](component: C)
  infix def deployedOn(device: D)(using device.Capabilities <:< component.Capabilities): Placement[C, D] =
    Placement(component, device)

extension [A <: Application[?], C <: Component[?, ?], D <: Device[?]](placement: Placement[C, D])
  infix def forDevice(device: A): Pippo[A, C, D] = Pippo(device, placement)

val deployment = MovementDetection deployedOn InfrastructuralWearable forDevice ApplicationSmartphone
