package it.unibo.capabilities

import it.unibo.capabilities.Language.Capability.{NotProvided, Provided}

import scala.util.NotGiven

object Language:
  trait TaggedCapability[+Cap](val cap: Cap)

  sealed trait Context
  final class DeviceContext extends Context
  final class RemoteContext extends Context

  enum Capability[+Cap]:
    case Provided(cap: Cap, ctx: DeviceContext)
    case NotProvided(ctx: RemoteContext)

  infix type requires[Result, Cap] = Capability[Cap] ?=> Result

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

  def myFunction: Unit requires A & B = function:
    capability[A | B] match
      case ab: (A & B) => println("I's an A and B")
      case aCap: A => println("I's an A")
      case bCap: B => println("I's a B")
    println(s"capability: ${summon[A | B]}")

  def myOtherFunction: Int requires B = function:
    capability[B].bar

  def pure =
    println("Pure function")
    10

  macroprogram(C()):
    pure
    myFunction
    myOtherFunction
