package it.unibo.mode2.language

import it.unibo.mode2.{Bar, Foo, MovementDetection}

sealed trait Device[ID: Ordering]:
  type Capabilities
trait Application[ID] extends Device[ID]
trait Infrastructural[ID] extends Device[ID]

trait Smartphone[ID] extends Application[ID]:
  override type Capabilities = Foo & Bar
trait Wearable[ID] extends Infrastructural[ID]:
  override type Capabilities = Any

object Smartphone1 extends Smartphone[1]
object Smartphone2 extends Smartphone[2]
object Wearable1 extends Wearable[1]

class Deployment[D <: Device[?], C <: Component[?, ?]](val device: D, val component: C)(using
    ev: device.Capabilities <:< component.Capabilities
)

trait A
trait B
trait C extends A with B
val a = Deployment(Smartphone1, MovementDetection)
