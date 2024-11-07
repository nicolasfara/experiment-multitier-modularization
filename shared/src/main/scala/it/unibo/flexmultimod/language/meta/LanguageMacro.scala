package it.unibo.flexmultimod.language.meta

import it.unibo.flexmultimod.language.FlexMultiModLanguage.{Placed, on}
import it.unibo.flexmultimod.tier.Peer

import scala.annotation.{MacroAnnotation, experimental}
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

@experimental
class modularized extends MacroAnnotation:
  override def transform(using quotes: Quotes)(
      definition: quotes.reflect.Definition,
      companion: Option[quotes.reflect.Definition]
  ): List[quotes.reflect.Definition] =
    import quotes.reflect._
    val newBody = definition match
      case DefDef(name, params, retType, Some(body)) =>
        val oldBody = body.asExpr
        val newBody = Expr.block(List(oldBody), '{ println(${ Expr(name) }) })
        DefDef.copy(definition)(name, params, retType, Some(newBody.asTerm))
      case _ => report.errorAndAbort("This annotation can only be applied to method definitions.")
    List(newBody)
