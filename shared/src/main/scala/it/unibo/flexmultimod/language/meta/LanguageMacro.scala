package it.unibo.flexmultimod.language.meta

import it.unibo.flexmultimod.language.FlexMultiModLanguage.{Placed, on}
import it.unibo.flexmultimod.tier.Peer

import scala.quoted.{Expr, Quotes, Type}

object LanguageMacro:
  inline def remoteRefMacro[Value, LocalNode <: Peer, RemoteNode <: Peer](func: Value on RemoteNode)(using placed: Placed[LocalNode]) =
    ${ remoteRefMacroImpl('func, 'placed) }

  private def remoteRefMacroImpl[Value, LocalNode <: Peer: Type, RemoteNode <: Peer: Type](
      expr: Expr[Value on RemoteNode],
      givenData: Expr[Placed[LocalNode]]
  )(using quote: Quotes): Expr[Value] =
    import quote.reflect.*
    val remoteType = TypeRepr.of[RemoteNode]
    val localType = TypeRepr.of[LocalNode]
    if (remoteType =:= localType)
      report.error(s"""
        Attempt to retrieve a remote value but it is placed locally at: ${localType.show}

        To retrieve a local value use the `bind` method instead.
      """.strip())
    '{ ??? }
