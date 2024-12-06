package it.unibo.flexmultimod.demo

import scala.compiletime.constValue
import it.unibo.flexmultimod.language.FlexMultiModLanguage.*
import it.unibo.flexmultimod.language.FlexMultiModLanguage.Language.*
import it.unibo.flexmultimod.language.{Aggregate, AggregateComponent, LocalComponent}
import it.unibo.flexmultimod.language.meta.modularized
import it.unibo.flexmultimod.tier.{Multiple, Peer}

trait WithAi
trait WithGps

object Module1 extends LocalComponent[Int *: EmptyTuple, String]:
  override type Constraints = Any
  override def apply[PlacedPeer <: Peer & Constraints](inputs: Int *: EmptyTuple): String on PlacedPeer =
    ???

object Module2 extends AggregateComponent[(String, Int), Double]:
  override type Constraints = Any
  override def apply[PlacedPeer <: Peer & Constraints](inputs: (String, Int))(using Aggregate): Double on PlacedPeer =
    ???

trait App extends Language:
  type Smartphone <: Peer { type Tie <: Multiple[EdgeServer] }
  type EdgeServer <: Peer { type Tie <: Multiple[Smartphone] }

  inline def macroProgram[Placement <: Peer](): Unit = programSpec[Placement]:
    val result = Module1[Smartphone](0 *: EmptyTuple)
    Module2[EdgeServer]("hello" *: 1 *: EmptyTuple)

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
