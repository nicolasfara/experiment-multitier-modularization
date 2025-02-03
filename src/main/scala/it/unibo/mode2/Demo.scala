package it.unibo.mode2

import it.unibo.mode2.language.{Application, Component, Infrastructural}

trait Aggregate[R]

trait WithAccelerometer
trait WithGps
trait WithAlert

object MovementDetection extends Component[EmptyTuple, Double]:
  override type Capabilities = WithAccelerometer | WithGps
  override def apply(input: EmptyTuple): Double = ???

object DistanceBetween extends Component[EmptyTuple, Double]:
  override type Capabilities = WithGps
  override def apply(input: EmptyTuple): Double = ???

object EmergencyDetection extends Component[Double *: Double *: EmptyTuple, Aggregate[Boolean]]:
  override type Capabilities = WithAlert
  override def apply(input: Double *: Double *: EmptyTuple): Aggregate[Boolean] = ???

trait Smartphone extends Application:
  override type Capabilities = WithAccelerometer & WithGps & WithAlert
  override type Tie <: Smartphone & Wearable

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

//object ApplicationSmartphone extends Smartphone
//object InfrastructuralWearable extends Wearable

import it.unibo.mode2.language.InfrastructuralDsl.*

def infrastructureSpecification(): Any =
  deployment:
    forDevice(Smartphone1):
      MovementDetection deployedOn Wearable1
      DistanceBetween deployedOn Smartphone1
      EmergencyDetection deployedOn Smartphone1
    forDevice(Smartphone2):
      MovementDetection deployedOn Wearable2
      DistanceBetween deployedOn Smartphone2
      EmergencyDetection deployedOn Smartphone2

def macroProgram(): Any =
  val movementSpeed = MovementDetection(EmptyTuple)
  val distance = DistanceBetween(EmptyTuple)
  EmergencyDetection(movementSpeed *: distance *: EmptyTuple)

// macroProgram executedOn infrastructureSpecification
