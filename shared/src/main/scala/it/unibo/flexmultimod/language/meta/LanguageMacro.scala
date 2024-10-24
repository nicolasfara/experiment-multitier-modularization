package it.unibo.flexmultimod.language.meta

import it.unibo.flexmultimod.language.FlexMultiModLanguage.{Placed, on}
import it.unibo.flexmultimod.tier.Peer

import scala.quoted.{Expr, Quotes, Type}

object LanguageMacro:
  inline def asLocalMacro[Value, LocalNode <: Peer, RemoteNode <: Peer](func: Value on RemoteNode)(using placed: Placed[LocalNode]) =
    ${ asLocalImpl('func, 'placed) }

  private def asLocalImpl[Value, LocalNode <: Peer: Type, RemoteNode <: Peer: Type](
      expr: Expr[Value on RemoteNode],
      givenData: Expr[Placed[LocalNode]]
  )(using quote: Quotes): Expr[Value] =
    import quote.reflect.*
    val remoteType = TypeRepr.of[RemoteNode]
    val localType = TypeRepr.of[LocalNode]
    if (remoteType =:= localType)
      report.error(s"""
        The value is placed at peer ${remoteType.show} which is also the current placement (${localType.show}).

        To retrieve a value placed on the local placement use the `bind` method instead.
      """.strip())
    '{ ??? }
