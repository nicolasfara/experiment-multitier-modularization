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

object WalkDetection extends Component[EmptyTuple, Double]:
  override type RequiredCapabilities = (WithGps | WithAccelerometer) & (WithSmartphone | WithWearable)
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](
      inputs: EmptyTuple
  ): Double on PlacedPeer = ???

object HeartbeatSensing extends Component[EmptyTuple, Int]:
  override type RequiredCapabilities = WithWearable
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](
      inputs: EmptyTuple
  ): Int on PlacedPeer = ???

object CollectiveEmergency extends Component[Double *: Int *: EmptyTuple, Aggregate[Boolean]]:
  override type RequiredCapabilities = Any
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](
      inputs: Double *: Int *: EmptyTuple
  ): Aggregate[Boolean] on PlacedPeer = ???

object ShowAlert extends Component[Boolean *: EmptyTuple, Unit]:
  override type RequiredCapabilities = WithSmartphone
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](
      inputs: Boolean *: EmptyTuple
  ): Unit on PlacedPeer = ???

object MacroApp:
  type Smartphone <: ApplicationPeer & WithSmartphone:
    type Tie <: Multiple[Cloud] & Single[Cloud]
  type Wearable <: InfrastructuralPeer & WithWearable:
    type Tie <: Single[Smartphone]
  type Cloud <: InfrastructuralPeer & WithAi:
    type Tie <: Multiple[Cloud] & Multiple[Smartphone]

  def macroProgram[Placement <: Peer](using Platform[Placement]): Macroprogram =
    program[Placement, Unit]:
      val walking = WalkDetection[Smartphone](EmptyTuple).placed
      val heartBeat = HeartbeatSensing[Wearable](EmptyTuple).placed
      val emergency = CollectiveEmergency[Cloud](walking *: heartBeat *: EmptyTuple).asLocallyPlaced
      ShowAlert[Smartphone](emergency *: EmptyTuple).placed
