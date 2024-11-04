package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.language.FlexMultiModLanguage.*
import it.unibo.flexmultimod.language.FlexMultiModLanguage.Language.*
import it.unibo.flexmultimod.tier.{Multiple, Peer}

trait WithAi
trait WithRam[RamGb <: Int]
trait WithTempSensor

trait Gradient[Node <: Peer & WithAi]:
  def gradientCast[Value](source: Boolean, value: Value): Value on Node

object Gradient:
  def apply[Node <: Peer & WithAi](): Gradient[Node] = new Gradient[Node]():
    def gradientCast[Value](source: Boolean, value: Value): Value on Node = ???

trait GreaterDistance[Node <: Peer & (WithTempSensor | WithRam[1])]:
  def isGreaterThan[Value](value: Value, threshold: Value): Boolean on Node

object GreaterDistance:
  def apply[Node <: Peer & (WithTempSensor | WithRam[1])](): GreaterDistance[Node] = new GreaterDistance[Node]():
    def isGreaterThan[Value](value: Value, threshold: Value): Boolean on Node = ???

trait Infrastructure:
  type Application <: Peer & WithRam[1] {type Tie <: Multiple[Infrastructural]}
  type Infrastructural <: Peer & WithAi {type Tie <: Multiple[Application]}

trait MyApplication extends Language:
  type Application <: Peer & WithRam[1] { type Tie <: Multiple[Infrastructural] }
  type Infrastructural <: Peer & WithAi { type Tie <: Multiple[Application] }

  private val gradient = Gradient[Infrastructural]()
  private val greaterDistance = GreaterDistance[Application]()

  def macroProgram(): Unit = program[Application]:
    val value = gradient.gradientCast(true, 10.0).remoteRef
//    greaterDistance.isGreaterThan(10.3, 5.0).remoteRef
    val bar = greaterDistance.isGreaterThan(value, 5.0).bind
