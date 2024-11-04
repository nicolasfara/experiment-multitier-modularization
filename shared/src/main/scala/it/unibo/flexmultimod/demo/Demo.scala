package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.language.FlexMultiModLanguage.*
import it.unibo.flexmultimod.language.FlexMultiModLanguage.Language.*
import it.unibo.flexmultimod.tier.{Multiple, Peer}
import scala.compiletime.constValue
import scala.compiletime.ops.int.*

trait WithAi
trait WithGps

trait Gradient[Node <: Peer & WithGps]:
  def gradientCast[Value](source: Boolean, value: Value): Value on Node

object Gradient:
  def apply[Node <: Peer & WithGps](): Gradient[Node] = new Gradient[Node]():
    def gradientCast[Value](source: Boolean, value: Value): Value on Node = ???

trait GreaterDistance[Node <: Peer]:
  def isGreaterThan[Value](value: Value, threshold: Value): Boolean on Node

object GreaterDistance:
  def apply[Node <: Peer](): GreaterDistance[Node] = new GreaterDistance[Node]():
    def isGreaterThan[Value](value: Value, threshold: Value): Boolean on Node = ???

trait HeavyComputation[Node <: Peer & WithAi]:
  def compute[Value](value: Value): Value on Node

object HeavyComputation:
  def apply[Node <: Peer & WithAi](): HeavyComputation[Node] = new HeavyComputation[Node]():
    def compute[Value](value: Value): Value on Node = ???

trait MyApplication extends Language:
  type Node <: Peer { type Tie <: Multiple[Node] }
  type Application <: Node { type Tie <: Multiple[Infrastructural] }
  type Infrastructural <: Node { type Tie <: Multiple[Application] }
  type EdgeServer <: Infrastructural & WithAi
  type CloudInstance <: Infrastructural
  type Smartphone <: Application & WithGps

  private val gradient = Gradient[Smartphone]()
  private val greaterDistance = GreaterDistance[CloudInstance]()
  private val heavyComputation = HeavyComputation[EdgeServer]()

  def macroProgram(): Unit = program[Smartphone]:
    val value = gradient.gradientCast(true, 10.0).bind
    val bar = greaterDistance.isGreaterThan(value, 5.0).remoteRef
    val result = heavyComputation.compute(bar).remoteRef
    println(result)
