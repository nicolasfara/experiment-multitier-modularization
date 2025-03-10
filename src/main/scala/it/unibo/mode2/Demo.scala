package it.unibo.mode2

import scala.concurrent.duration.*
import it.unibo.mode2.language.deployment.{Application, Infrastructural}
import it.unibo.mode2.model.component.{CollectiveComponent, LocalComponent}
import it.unibo.mode2.model.scheduling.{DeferredScheduling, Periodic, SchedulingPolicy}
import it.unibo.mode2.platform.Context

case class Coordinate(latitude: Double, longitude: Double)
case class Axis(x: Double, y: Double, z: Double)

// Component Capabilities ------------------------------------------------

/** Behavioral capabilities
  */
trait Accelerometer:
  def readAccelerometer: Axis

trait Gps:
  def getPosition: Coordinate

trait HighComputation

trait Alert:
  def sendAlert(dangerZone: Boolean): Unit

trait HeartbeatSensor:
  def heartbeat: Int

// Component Definitions ------------------------------------------------

object PositionSensor extends LocalComponent[EmptyTuple, Coordinate], DeferredScheduling:
  override type Capabilities = Accelerometer | Gps

  override def schedulingPolicy(using Context): SchedulingPolicy = withCapability:
    case _: Accelerometer => Periodic(100.milliseconds)
    case _: Gps           => Periodic(1.second)

  override def apply(input: EmptyTuple): Context ?=> Coordinate =
    val result: Coordinate = withCapability:
      case accelerometer: Accelerometer =>
        accelerometer.readAccelerometer
        ???
      case gps: Gps =>
        gps.getPosition
        ???
    result
end PositionSensor

object HeartbeatAcquisition extends LocalComponent[EmptyTuple, Int], Periodic(1.second):
  override type Capabilities = HeartbeatSensor
  override def apply(input: EmptyTuple): Context ?=> Int = withCapability:
    case heartbeatSensor: HeartbeatSensor => heartbeatSensor.heartbeat
end HeartbeatAcquisition

object RegionsHeartbeatDetection extends CollectiveComponent[(Coordinate, Int), Boolean], Periodic(5.seconds):
  override type Capabilities = HighComputation
  override def apply(input: (Coordinate, Int)): Context ?=> Boolean =
    val neighbors = sharedData.neighborValues
    // Compute the heartbeat in the zone
    ???
end RegionsHeartbeatDetection

object AlertActuation extends LocalComponent[Boolean *: EmptyTuple, Unit], Periodic(2.seconds):
  override type Capabilities = Alert
  override def apply(input: Boolean *: EmptyTuple): Context ?=> Unit = withCapability:
    case alert: Alert => alert.sendAlert(input.head)
end AlertActuation

// Infrastructure Definitions --------------------------------------------

trait Smartphone extends Application:
  override type Capabilities = Accelerometer & Gps & Alert
  override type Tie <: Smartphone & Wearable & Edge

trait Wearable extends Infrastructural:
  override type Capabilities = Accelerometer & HeartbeatSensor
  override type Tie <: Wearable & Smartphone

trait Edge extends Infrastructural:
  override type Capabilities = HighComputation
  override type Tie <: Edge & Smartphone

object Smartphone1 extends Smartphone
object Smartphone2 extends Smartphone
object Wearable1 extends Wearable
object Wearable2 extends Wearable
object Edge1 extends Edge

//object ApplicationSmartphone extends Smartphone
//object InfrastructuralWearable extends Wearable

import it.unibo.mode2.language.deployment.InfrastructuralDsl.*

def infrastructureSpecification(): Any =
  deployment:
    forDevice(Smartphone1):
      PositionSensor deployedOn Wearable1
      HeartbeatAcquisition deployedOn Wearable1
      RegionsHeartbeatDetection deployedOn Edge1
      AlertActuation deployedOn Smartphone1
    forDevice(Smartphone2):
      PositionSensor deployedOn Smartphone2
      HeartbeatAcquisition deployedOn Wearable2
      RegionsHeartbeatDetection deployedOn Edge1
      AlertActuation deployedOn Smartphone2

def macroProgram(using Context): Any =
  val position = PositionSensor(EmptyTuple)
  val heartbeat = HeartbeatAcquisition(EmptyTuple)
  val alert = RegionsHeartbeatDetection(position *: heartbeat *: EmptyTuple)
  AlertActuation(alert *: EmptyTuple)

// macroProgram executedOn infrastructureSpecification
