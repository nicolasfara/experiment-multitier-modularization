package it.unibo.mode2

import it.unibo.mode2.language.{Application, CollectiveComponent, Component, Infrastructural, LocalComponent, SharedData}

trait Aggregate[R] extends SharedData[R]

trait WithAccelerometer
trait WithGps
trait WithAlert

object PositionSensor extends LocalComponent[EmptyTuple, (Double, Double)]:
  override type Capabilities = WithAccelerometer | WithGps
  override def apply(input: EmptyTuple): (Double, Double) = ???

object HeartbeatSensor extends LocalComponent[EmptyTuple, Int]:
  override type Capabilities = WithAccelerometer
  override def apply(input: EmptyTuple): Int = ???

object RegionsHeartbeatDetection extends CollectiveComponent[(Double, Double) *: Int *: EmptyTuple, Aggregate[Boolean]]:
  override type Capabilities = WithAlert
  override def apply(input: (Double, Double) *: Int *: EmptyTuple): Aggregate[Boolean] = ???

object AlertActuation extends LocalComponent[Boolean *: EmptyTuple, Unit]:
  override type Capabilities = WithGps
  override def apply(input: Boolean *: EmptyTuple): Unit = ???

trait Smartphone extends Application:
  override type Capabilities = WithAccelerometer & WithGps & WithAlert
  override type Tie <: Smartphone & Wearable & Edge

trait Wearable extends Infrastructural:
  override type Capabilities = WithAccelerometer
  override type Tie <: Wearable & Smartphone

trait Edge extends Infrastructural:
  override type Capabilities = Any
  override type Tie <: Edge & Smartphone

object Smartphone1 extends Smartphone
object Smartphone2 extends Smartphone
object Wearable1 extends Wearable
object Wearable2 extends Wearable
object Edge1 extends Edge

//object ApplicationSmartphone extends Smartphone
//object InfrastructuralWearable extends Wearable

import it.unibo.mode2.language.InfrastructuralDsl.*

def infrastructureSpecification(): Any =
  deployment:
    forDevice(Smartphone1):
      PositionSensor deployedOn Wearable1
      AlertActuation deployedOn Smartphone1
      RegionsHeartbeatDetection deployedOn Smartphone1
    forDevice(Smartphone2):
      PositionSensor deployedOn Wearable2
      AlertActuation deployedOn Smartphone2
      RegionsHeartbeatDetection deployedOn Smartphone2

def macroProgram(): Any =
  val position = PositionSensor(EmptyTuple)
  val heartbeat = HeartbeatSensor(EmptyTuple)
  val alert = RegionsHeartbeatDetection(position *: heartbeat *: EmptyTuple).local
  AlertActuation(alert *: EmptyTuple)

// macroProgram executedOn infrastructureSpecification
