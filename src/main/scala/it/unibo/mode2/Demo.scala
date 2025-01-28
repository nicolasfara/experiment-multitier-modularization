package it.unibo.mode2

import it.unibo.mode2.language.{Application, Component, Infrastructural}

trait WithAccelerometer
trait WithGps
trait WithAlert

object MovementDetection extends Component[EmptyTuple, Double]:
  override type Capabilities >: WithAccelerometer | WithGps
  override def apply(input: EmptyTuple): Double = ???

object DistanceBetween extends Component[EmptyTuple, Double]:
  override type Capabilities >: WithGps
  override def apply(input: EmptyTuple): Double = ???

object EmergencyDetection extends Component[Double *: Double *: EmptyTuple, Boolean]:
  override type Capabilities >: WithAlert
  override def apply(input: Double *: Double *: EmptyTuple): Boolean = ???

trait Smartphone[ID] extends Application[ID]:
  override type Capabilities = WithAccelerometer & WithGps & WithAlert
trait Wearable[ID] extends Infrastructural[ID]:
  override type Capabilities = WithAccelerometer

object ApplicationSmartphone extends Smartphone[1]
object InfrastructuralWearable extends Wearable[1]

def infrastructureSpecification(): Unit =
  import it.unibo.mode2.language.InfrastructuralDsl.*
  MovementDetection deployedOn InfrastructuralWearable forDevice ApplicationSmartphone
  DistanceBetween deployedOn ApplicationSmartphone forDevice ApplicationSmartphone
  EmergencyDetection deployedOn ApplicationSmartphone forDevice ApplicationSmartphone

def macroProgram(): Unit =
  val movementSpeed = MovementDetection(EmptyTuple)
  val distance = DistanceBetween(EmptyTuple)
  EmergencyDetection(movementSpeed *: distance *: EmptyTuple)
