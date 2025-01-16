package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.core.Cardinality.*
import it.unibo.flexmultimod.core.{ApplicationPeer, Component, InfrastructuralPeer, Peer}
import it.unibo.flexmultimod.core.language.{Aggregate, Language, Macroprogram}
import it.unibo.flexmultimod.platform.Platform
import it.unibo.flexmultimod.core.language.Language.*

trait WithAi
trait WithGps
trait WithAccelerometer
trait WithNotification

object WalkDetection extends Component[EmptyTuple, Double]:
  override type RequiredCapabilities = WithGps & WithAccelerometer
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmptyTuple): Double = ???

object HeartbeatSensing extends Component[EmptyTuple, Int]:
  override type RequiredCapabilities = WithAccelerometer
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmptyTuple): Int = ???

/* Collective component requiring the interaction with neighbor devices executing the same component instance.
 * Note the Aggregate[Boolean] return type, which represents the collective nature of the component.
 */
object CollectiveEmergency extends Component[Double *: Int *: EmptyTuple, Aggregate[Boolean]]:
  override type RequiredCapabilities = Any
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](
      inputs: Double *: Int *: EmptyTuple
  ): Aggregate[Boolean] = ???

object ShowAlert extends Component[Boolean *: EmptyTuple, Unit]:
  override type RequiredCapabilities = WithNotification
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: Boolean *: EmptyTuple): Unit = ???

object MacroApp:
  type Smartphone <: ApplicationPeer & WithGps & WithAccelerometer & WithNotification:
    type Tie <: Single[Cloud] & Single[Wearable]
  type Wearable <: InfrastructuralPeer & WithAccelerometer:
    type Tie <: Single[Smartphone]
  type Cloud <: InfrastructuralPeer & WithAi:
    type Tie <: Multiple[Cloud] & Multiple[Smartphone]

  def macroProgram[Placement <: Peer](using Platform[Placement]): Macroprogram =
    program[Placement, Unit]:
      val walking = WalkDetection[Smartphone](EmptyTuple)
      val heartBeat = HeartbeatSensing[Wearable](EmptyTuple)
      val emergency = CollectiveEmergency[Cloud](walking *: heartBeat *: EmptyTuple).localValue
      ShowAlert[Smartphone](emergency *: EmptyTuple)
