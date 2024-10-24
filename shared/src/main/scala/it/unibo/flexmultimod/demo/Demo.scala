package it.unibo.fleximultimod.demo

import it.unibo.fleximultimod.language.Language.*
import it.unibo.fleximultimod.language.Language.given
import it.unibo.fleximultimod.tier.Cardinality.{Multiple, Single}
import it.unibo.fleximultimod.tier.{
  PhysicalArchitecture,
  PhysicalNode,
  TiedWith
}

trait Gradient[Node]:
  def gradientCast[Value](source: Boolean, value: Value): Value on Node
end Gradient

object Gradient:
  def apply[Node](): Gradient[Node] = new Gradient[Node]():
    def gradientCast[Value](source: Boolean, value: Value): Value on Node = ???

trait GreaterDistance[Node]:
  def isGreaterThan[Value](
      value: Value,
      threshold: Value
  ): Boolean on Node
end GreaterDistance

object GreaterDistance:
  def apply[Node](): GreaterDistance[Node] =
    new GreaterDistance[Node]():
      def isGreaterThan[Value](
          value: Value,
          threshold: Value
      ): Boolean on Node = ???

trait ApplicationDevice
trait InfrastructuralDevice

trait SingleInfrastructural extends ApplicationDevice, InfrastructuralDevice

trait MyApplication extends SingleInfrastructural:
  private val gradient = Gradient[InfrastructuralDevice]()
  private val greaterDistance = GreaterDistance[ApplicationDevice]()

  def macroProgram() =
    program[ApplicationDevice]:
      val po = gradient.gradientCast(true, 10.0)
      val foo = gradient.gradientCast(true, 10.0).asLocal
//      greaterDistance.isGreaterThan(10.3, 5.0).asLocal

      val bar: Boolean = greaterDistance.isGreaterThan(10.3, 5.0)
      ???
