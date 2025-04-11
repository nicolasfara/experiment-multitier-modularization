package it.unibo.capabilities

import it.unibo.capabilities.Language.Capability.{NotProvided, Provided}

import scala.util.NotGiven

object Language:
  trait TaggedCapability[+Cap](val cap: Cap)

  sealed trait Context
  final class DeviceContext extends Context
  final class RemoteContext extends Context

  infix type requires[Result, Cap] = Capability[Cap] ?=> Result

  enum Capability[+Cap]:
    case Provided(cap: Cap, ctx: DeviceContext)
    case NotProvided(ctx: RemoteContext)

  infix type :|[Place <: Cap, Cap] = Capability[Cap] ?=> Place

  private case class On[Result, Refined](result: Result)

  infix opaque type on[Result, Refined] = On[Result, Refined]

  extension [Result, Refined](on: on[Result, Refined])
    def asLocal: Result = on.result

  def withCapability[Result, Place <: Cap, Cap](using Capability[Cap])(body: Cap ?=> Result): on[Result, Place :| Cap] = ???

  def plain[Result, Place](body: => Result): on[Result, Place] = ???

  class Scope

  def macroprogram[Cap](capability: Cap)(body: (TaggedCapability[Cap], Scope) ?=> Unit): Unit =
    given TaggedCapability[Cap](capability)
    given Scope()
    body

  def capability[Cap](using cap: Capability[Cap]): Cap = cap match
    case Provided(capability, _) => capability
    case NotProvided(_)          => throw new Exception("Capability not provided")

  def function[Cap, Output](using cap: Capability[Cap])(body: Cap ?=> Output): Output = cap match
    case Provided(capability, ctx) => println("Hey, I have the capabilities!"); body(using capability)
    case NotProvided(_)            => println("I don't have the capabilities, remote call!"); ???

  object Context:
    given [Cap] => TaggedCapability[Cap] => Provided[Cap] = Provided(summon[TaggedCapability[Cap]].cap, DeviceContext())
    given [Cap] => NotGiven[TaggedCapability[Cap]] => NotProvided[Cap] = NotProvided(RemoteContext())

@main def main(): Unit =
  import Language.*
  import Language.Context.given

  trait A:
    def foo: String = "Hello"
  trait B:
    def bar: Int = 10
  class C extends A, B

  def myfunction(): Int on C :| B = withCapability:
    capability[C].foo
    12

  def otherFunction(input: Int): String on C = plain:
    s"Hello $input"

  macroprogram(C()):
    val result = myfunction()
    otherFunction(result.asLocal)
