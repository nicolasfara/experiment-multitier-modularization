package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.core.Cardinality.*
import it.unibo.flexmultimod.core.{Component, Peer}
import it.unibo.flexmultimod.core.language.{Aggregate, Macroprogram, on}
import it.unibo.flexmultimod.platform.Platform
import it.unibo.flexmultimod.core.language.Language.*

//import scala.compiletime.constValue
//import it.unibo.flexmultimod.language.FlexMultiModLanguage.*
//import it.unibo.flexmultimod.language.meta.modularized
//import it.unibo.flexmultimod.platform.{Macroprogram, Platform}
//import it.unibo.flexmultimod.tier.Cardinality.{Multiple, Single}
//import it.unibo.flexmultimod.tier.Peer

trait WithAi
trait WithGps

object Module1 extends Component[Int *: EmptyTuple, String]:
  override type RequiredCapabilities = Any
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: Int *: EmptyTuple): String on PlacedPeer =
    ???

object Module2 extends Component[String *: Int *: EmptyTuple, Double], Aggregate:
  override type RequiredCapabilities = Any
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](
      inputs: String *: Int *: EmptyTuple
  ): Double on PlacedPeer = ???

object MacroApp:
  type Smartphone <: Peer { type Tie <: Multiple[EdgeServer] & Single[Cloud] }
  type EdgeServer <: Peer { type Tie <: Multiple[Smartphone] & Single[Cloud] }
  type Cloud <: Peer { type Tie <: Multiple[EdgeServer] & Multiple[Smartphone] }

  def macroProgram[Placement <: Peer](using Platform[Placement]): Macroprogram =
    program[Placement, Unit]:
      val result = Module1[Smartphone](0 *: EmptyTuple).placed
      Module2[EdgeServer](result *: 1 *: EmptyTuple).placed
      ()

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
