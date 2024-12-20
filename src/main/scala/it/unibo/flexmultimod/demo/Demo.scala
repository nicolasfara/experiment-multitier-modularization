package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.core.Cardinality.*
import it.unibo.flexmultimod.core.{ApplicationPeer, Component, InfrastructuralPeer, Peer}
import it.unibo.flexmultimod.core.language.{Aggregate, Language, Macroprogram}
import it.unibo.flexmultimod.platform.Platform
import it.unibo.flexmultimod.core.language.Language.*

trait WithAi
trait WithGps
trait WithAccelerometer
trait WithSmartphone extends WithGps, WithAccelerometer
trait WithWearable extends WithAccelerometer

object WalkDetection extends Component[EmptyTuple, Double, WithGps | WithAccelerometer]:
  override def apply[PlacedPeer <: WithRequiredCapabilities](inputs: EmptyTuple): Double = ???

object HeartbeatSensing extends Component[EmptyTuple, Int, WithWearable]:
  override def apply[PlacedPeer <: WithRequiredCapabilities](inputs: EmptyTuple): Int = ???

object CollectiveEmergency extends Component[Double *: Int *: EmptyTuple, Aggregate[Boolean], Any]:
  override def apply[PlacedPeer <: WithRequiredCapabilities](inputs: Double *: Int *: EmptyTuple): Aggregate[Boolean] =
    ???

object ShowAlert extends Component[Boolean *: EmptyTuple, Unit, WithSmartphone]:
  override def apply[PlacedPeer <: WithRequiredCapabilities](inputs: Boolean *: EmptyTuple): Unit = ???

object MacroApp:
  type Smartphone <: ApplicationPeer & WithSmartphone { type Tie <: Single[Cloud] & Single[Wearable] }
  type Wearable <: InfrastructuralPeer & WithWearable { type Tie <: Single[Smartphone] }
  type Cloud <: InfrastructuralPeer & WithAi { type Tie <: Multiple[Cloud] & Multiple[Smartphone] }

  def macroProgram[Placement <: Peer](using Platform[Placement]): Macroprogram =
    program[Placement, Unit]:
      val walking = WalkDetection[Smartphone](EmptyTuple)
      val heartBeat = HeartbeatSensing[Wearable](EmptyTuple)
      val emergency = CollectiveEmergency[Cloud](walking *: heartBeat *: EmptyTuple).localValue
      ShowAlert[Smartphone](emergency *: EmptyTuple)
