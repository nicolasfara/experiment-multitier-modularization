package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.language.FlexMultiModLanguage.*
import it.unibo.flexmultimod.language.FlexMultiModLanguage.Language.*
import it.unibo.flexmultimod.language.meta.modularized
import it.unibo.flexmultimod.tier.{Multiple, Peer}

import scala.compiletime.constValue

trait WithAi
trait WithGps

trait Gradient:
  def gradientCast[Node <: Peer](source: Boolean, value: Double): Double on Node = ???

trait GreaterDistance:
  def isGreaterThan[Node <: Peer](value: Double, threshold: Double): Boolean on Node = ???

trait HeavyComputation:
  def compute[Value, Node <: Peer & WithAi](value: Value): Value on Node = ???

trait MyApplication extends Language, Gradient, GreaterDistance, HeavyComputation:
  type Node <: Peer { type Tie <: Multiple[Node] }
  type Application <: Node { type Tie <: Multiple[Infrastructural] }
  type Infrastructural <: Node { type Tie <: Multiple[Application] }
  type EdgeServer <: Infrastructural & WithAi
  type CloudInstance <: Infrastructural
  type Smartphone <: Application & WithGps

  @modularized def macroProgram(): Unit =
    program[Smartphone]:
      val value = gradientCast[Smartphone](true, 10.0).bind
      val bar = isGreaterThan[EdgeServer](value, 5.0).remoteRef
      val result = compute[Boolean, EdgeServer](bar).remoteRef
      println(result)
