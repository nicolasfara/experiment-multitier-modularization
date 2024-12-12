package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.core.Cardinality.*
import it.unibo.flexmultimod.core.{Component, Peer}
import it.unibo.flexmultimod.core.language.{Aggregate, Macroprogram, on}
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

object HeartBeatSensing extends Component[EmptyTuple, Int]:
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
  type Smartphone <: Peer & WithSmartphone { type Tie <: Multiple[Cloud] & Single[Cloud] }
  type Wearable <: Peer & WithWearable { type Tie <: Single[Smartphone] }
  type Cloud <: Peer & WithAi { type Tie <: Multiple[Cloud] & Multiple[Smartphone] }

  def macroProgram[Placement <: Peer](using Platform[Placement]): Macroprogram =
    program[Placement, Unit]:
      val walking = WalkDetection[Smartphone](EmptyTuple).placed
      val heartBeat = HeartBeatSensing[Wearable](EmptyTuple).placed
      val emergency = CollectiveEmergency[Cloud](walking *: heartBeat *: EmptyTuple).asLocallyPlaced
      ShowAlert[Smartphone](emergency *: EmptyTuple).placed

//trait MyApplication extends Language, Gradient, GreaterDistance, HeavyComputation:
//  type Node <: Peer { type Tie <: Multiple[Node] }
//  type Application <: Node { type Tie <: Multiple[Infrastructural] }
//  type Infrastructural <: Node { type Tie <: Multiple[Application] }
//  type EdgeServer <: Infrastructural & WithAi
//  type CloudInstance <: Infrastructural
//  type Smartphone <: Application & WithGps
//
//  @modularized def macroProgram(): Unit =
//    program[Smartphone]:
//      val value = gradientCast[Smartphone](true, 10.0).bind
//      val bar = isGreaterThan[EdgeServer](value, 5.0).remoteRef
//      val result = compute[Boolean, EdgeServer](bar).remoteRef
//      println(result)
