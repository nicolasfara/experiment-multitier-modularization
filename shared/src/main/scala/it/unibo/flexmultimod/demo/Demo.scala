package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.language.FlexMultiModLanguage.*
import it.unibo.flexmultimod.language.FlexMultiModLanguage.Language.*
import it.unibo.flexmultimod.tier.Cardinality.Multiple
import it.unibo.flexmultimod.tier.Peer

trait Gradient[Node <: Peer]:
  def gradientCast[Value](source: Boolean, value: Value): Value on Node

object Gradient:
  def apply[Node <: Peer](): Gradient[Node] = new Gradient[Node]():
    def gradientCast[Value](source: Boolean, value: Value): Value on Node = ???

trait GreaterDistance[Node <: Peer]:
  def isGreaterThan[Value](value: Value, threshold: Value): Boolean on Node

object GreaterDistance:
  def apply[Node <: Peer](): GreaterDistance[Node] = new GreaterDistance[Node]():
    def isGreaterThan[Value](value: Value, threshold: Value): Boolean on Node = ???

trait ApplicationDevice extends Peer
trait InfrastructuralDevice extends Peer

trait SingleInfrastructural:
  type Application <: Peer { type Tie <: Multiple[Infrastructural] }
  type Infrastructural <: Peer { type Tie <: Multiple[Peer] }

trait MyApplication extends SingleInfrastructural, Language:
  private val gradient = Gradient[Infrastructural]()
  private val greaterDistance = GreaterDistance[Application]()

  def macroProgram(): Unit = program[Application]:
    val value = gradient.gradientCast(true, 10.0).remoteRef
//      greaterDistance.isGreaterThan(10.3, 5.0).remoteRef
    val bar = greaterDistance.isGreaterThan(value, 5.0).bind
