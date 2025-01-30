package it.unibo.mode2

import it.unibo.mode2.language.{Application, Component, Infrastructural}

trait WithAccelerometer
trait WithGps
trait WithAlert

object MovementDetection extends Component[EmptyTuple, Double]:
  override type Capabilities = WithAccelerometer | WithGps
  override def apply(input: EmptyTuple): Double = ???

object DistanceBetween extends Component[EmptyTuple, Double]:
  override type Capabilities = WithGps
  override def apply(input: EmptyTuple): Double = ???

object EmergencyDetection extends Component[Double *: Double *: EmptyTuple, Boolean]:
  override type Capabilities = WithAlert
  override def apply(input: Double *: Double *: EmptyTuple): Boolean = ???

trait Smartphone extends Application:
  override type Capabilities = WithAccelerometer & WithGps & WithAlert

trait Wearable extends Infrastructural:
  override type Capabilities = WithAccelerometer

object ApplicationSmartphone extends Smartphone
object InfrastructuralWearable extends Wearable

import it.unibo.mode2.language.InfrastructuralDsl.*

def infrastructureSpecification(): Any =
  deployment:
    forDevice(ApplicationSmartphone):
      MovementDetection deployedOn InfrastructuralWearable
      DistanceBetween deployedOn ApplicationSmartphone
      EmergencyDetection deployedOn ApplicationSmartphone

def macroProgram(): Any =
  val movementSpeed = MovementDetection(EmptyTuple)
  val distance = DistanceBetween(EmptyTuple)
  EmergencyDetection(movementSpeed *: distance *: EmptyTuple)

// macroProgram executedOn infrastructureSpecification
