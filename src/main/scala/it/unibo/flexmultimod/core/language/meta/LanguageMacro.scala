package it.unibo.flexmultimod.core.language.meta

import scala.annotation.{MacroAnnotation, experimental, targetName}
import scala.quoted.{Expr, Quotes, Type}

import it.unibo.flexmultimod.core.language.Language.*
import it.unibo.flexmultimod.core.{Component, Peer}
import it.unibo.flexmultimod.core.language.Macroprogram

object LanguageMacro:
  def programMacroImpl[PlacedOn <: Peer, Result](
      symbol: Expr[Placed[PlacedOn] ?=> Result]
  )(using quotes: Quotes): Expr[Macroprogram] =
    import quotes.reflect.*
    val programBody = extractProgramBody(using quotes)(symbol.asTerm) // Extract the program body
    noCodeBetweenComponentsOrRaise(using quotes)(programBody) // Check if there is code between components
    '{ new Macroprogram {} }

  private def noCodeBetweenComponentsOrRaise(using quotes: Quotes)(programBody: quotes.reflect.Block): Unit =
    import quotes.reflect.*
    programBody match
      case Block(statements, _) =>
        statements
          .map(stmt => stmt -> containsComponent(using quotes)(stmt))
          .filterNot: (_, isComponent) =>
            isComponent
          .foreach: (stmt, _) =>
            report.errorAndAbort(s"Code between components is not allowed: ${stmt.show}", stmt.pos)

  private def containsComponent(using quotes: Quotes)(statement: quotes.reflect.Statement): Boolean =
    import quotes.reflect.*
    statement match
      case ValDef(_, _, Some(rhs)) => containsComponent(rhs)
      case Apply(term, args)       => args.exists(containsComponent) || containsComponent(term)
      case Select(term, _)         => containsComponent(term)
      case TypeApply(term, args)   => args.exists(_.isComponent) || containsComponent(term)
      case id: Term                => id.isComponent
      case tt: TypeTree            => tt.isComponent
      case _                       => false

  extension (using quotes: Quotes)(tt: quotes.reflect.TypeTree)
    @targetName("isComponentTypeTree")
    def isComponent: Boolean =
      import quotes.reflect.*
      tt.tpe <:< TypeRepr.of[Component[?, ?]]

  extension (using quotes: Quotes)(tt: quotes.reflect.Term)
    @targetName("isComponentTerm")
    def isComponent: Boolean =
      import quotes.reflect.*
      tt.tpe <:< TypeRepr.of[Component[?, ?]]

  private def extractProgramBody(using quotes: Quotes)(programBody: quotes.reflect.Statement): quotes.reflect.Block =
    import quotes.reflect.*
    programBody match
      case Inlined(_, _, Block(DefDef(_, _, _, Some(Inlined(_, _, blockBody: Block))) :: Nil, _)) => blockBody
      case _ => report.errorAndAbort("The program must be defined inside a block.")

//  inline def remoteRefMacro[Value, LocalNode <: Peer, RemoteNode <: Peer](func: Value on RemoteNode)(using
//      placed: Placed[LocalNode]
//  ) = ${ remoteRefMacroImpl('func, 'placed) }
//
//  private def remoteRefMacroImpl[Value, LocalNode <: Peer: Type, RemoteNode <: Peer: Type](
//      expr: Expr[Value on RemoteNode],
//      givenData: Expr[Placed[LocalNode]]
//  )(using quote: Quotes): Expr[Value] =
//    import quote.reflect.*
//    val remoteType = TypeRepr.of[RemoteNode]
//    val localType = TypeRepr.of[LocalNode]
//    if (remoteType =:= localType)
//      report.error(s"""
//        Attempt to retrieve a remote value but it is placed locally at: ${localType.show}
//
//        To retrieve a local value use the `bind` method instead.
//      """.strip())
//    '{ ??? }
//
//@experimental
//class modularized extends MacroAnnotation:
//  override def transform(using quotes: Quotes)(
//      definition: quotes.reflect.Definition,
//      companion: Option[quotes.reflect.Definition]
//  ): List[quotes.reflect.Definition] =
//    import quotes.reflect._
//    val newBody = definition match
//      case DefDef(name, params, retType, Some(body)) =>
//        val oldBody = body.asExpr
//        val newBody = Expr.block(List(oldBody), '{ println(${ Expr(name) }) })
//        DefDef.copy(definition)(name, params, retType, Some(newBody.asTerm))
//      case _ => report.errorAndAbort("This annotation can only be applied to method definitions.")
//    List(newBody)
