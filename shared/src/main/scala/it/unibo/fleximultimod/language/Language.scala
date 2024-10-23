package it.unibo.fleximultimod.language

import it.unibo.fleximultimod.language.Language.Placed
import it.unibo.fleximultimod.tier.PhysicalNode

import scala.compiletime.{erasedValue, error}
import scala.quoted.{Expr, Quotes, Type}

infix type on[Value, RemoteNode <: PhysicalNode] = Value

object Language:
  trait Placed[Node]
  def program[Node](programScope: Placed[Node] ?=> Unit): Unit = ???

  inline def asLocal[
      Value,
      LocalNode <: PhysicalNode,
      RemoteNode <: PhysicalNode
  ](func: Unit => Value on RemoteNode)(using Placed[LocalNode]): Value = ${ asLocalImpl }

  private def asLocalImpl[Val, RemNode: Type, LocalNode: Type](using Quotes): Expr[Val] = ???

//type A = Int | String
//inline def foo(): A = 42
//transparent inline def foo2(): A = 42
//
//object Language:
//  val x2: Int = foo()
//  val x: Int = foo2()
