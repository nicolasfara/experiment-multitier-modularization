package it.unibo.fleximultimod.language

import scala.quoted.{Expr, Quotes, Type}
import scala.compiletime.{erasedValue, error}

object Language:
  trait Placed[+Node]
  infix final case class on[Value, RemoteNode]()

  given foo[Value, Node: Placed]: Conversion[on[Value, Node], Value] with
    def apply(value: on[Value, Node]): Value = ???

  def program[Node](programScope: Placed[Node] ?=> Unit): Unit =
    ???

  extension [Value, LocalNode](value: Value on LocalNode)
    def bind: Placed[LocalNode] ?=> Value = ???

  extension [Value, LocalNode, RemoteNode](value: Value on RemoteNode)
    inline def asLocal(using placed: Placed[LocalNode]): Value = asLocalMacro(
      value
    )

  private inline def asLocalMacro[Value, LocalNode, RemoteNode](
      func: Value on RemoteNode
  )(using placed: Placed[LocalNode]) = ${
    asLocalImpl('func, 'placed)
  }

  private def asLocalImpl[Value, LocalNode: Type, RemoteNode: Type](
      expr: Expr[Value on RemoteNode],
      givenData: Expr[Placed[LocalNode]]
  )(using quote: Quotes): Expr[Value] =
    import quote.reflect.*
    val remoteType = TypeRepr.of[RemoteNode]
    val localType = TypeRepr.of[LocalNode]
    if (remoteType =:= localType)
      '{ error("foo") }
    else
      '{ ??? }

//type A = Int | String
//inline def foo(): A = 42
//transparent inline def foo2(): A = 42
//
//object Language:
//  val x2: Int = foo()
//  val x: Int = foo2()
