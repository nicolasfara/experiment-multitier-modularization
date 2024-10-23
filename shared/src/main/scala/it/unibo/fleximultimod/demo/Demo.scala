package it.unibo.fleximultimod.demo

import it.unibo.fleximultimod.language.{Language, on}
import it.unibo.fleximultimod.tier.Cardinality.{Multiple, Single}
import it.unibo.fleximultimod.tier.{
  PhysicalArchitecture,
  PhysicalNode,
  TiedWith
}

trait Gradient[+Node <: PhysicalNode]:
  def gradientCast[Value](source: Boolean, value: Value): Value on Node
end Gradient

object Gradient:
  def apply[Node <: PhysicalNode](): Gradient[Node] = new Gradient[Node]():
    def gradientCast[Value](source: Boolean, value: Value): Value on Node = ???

trait GreaterDistance[+Node <: PhysicalNode]:
  def isGreaterThan[Value](
      value: Value,
      threshold: Value
  ): Boolean on Node
end GreaterDistance

object GreaterDistance:
  def apply[Node <: PhysicalNode](): GreaterDistance[Node] =
    new GreaterDistance[Node]():
      def isGreaterThan[Value](
          value: Value,
          threshold: Value
      ): Boolean on Node = ???

trait SingleInfrastructural:
  type Application <: PhysicalNode
  type Infrastructural <: PhysicalNode
  type AppTie = Application TiedWith Single[Infrastructural]
  type InfraTie = Infrastructural TiedWith Multiple[Application]

trait MyApplication extends SingleInfrastructural:
  private val gradient = Gradient[Infrastructural]()
  private val greaterDistance = GreaterDistance[Application]()

  def macroProgram() = program[Application]:
    val value = gradient.gradientCast(true, 10.0).asLocal
    val isDistant = greaterDistance.isGreaterThan(10.3, 5.0).asLocal
    ???
