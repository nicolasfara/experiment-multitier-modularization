package it.unibo.flexmultimod.core.language.meta

import scala.annotation.{MacroAnnotation, experimental}
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
    programBody match
      case Block(statements, returnTerm) =>
        statements.flatMap:
          case ValDef(name, tpt, Some(rhs)) => getTypes(using quotes)(rhs)
        .foreach: elem =>
          report.info(elem.toString)
    codeBetweenComponents(symbol.asTerm) // Check if the code is between components
    '{ new Macroprogram {} }

  private def codeBetweenComponents[PlacedOn <: Peer, Result](using quotes: Quotes)(
      programBody: quotes.reflect.Term
  ): Boolean = true

  /**
   * Search recursively accumulating all the types in the tree.
   * @param quotes
   * @param term
   * @return
   */
  private def getTypes(using quotes: Quotes)(term: quotes.reflect.Term): List[quotes.reflect.TypeRepr] =
    import quotes.reflect.*
    term match
      case Apply(fun, args) => getTypes(fun) ++ args.flatMap(getTypes)
      case TypeApply(fun, args) => getTypes(fun) ++ args.map(_.tpe)
      case Select(qualifier, _) => getTypes(qualifier)
      case Ident(_) => List(term.tpe)
      case Inlined(_, bindings, expansion) => bindings.flatMap:
        case ValDef(_, tt, Some(rhs)) => getTypes(rhs) ++ List(tt.tpe)
      case Block(stats, expr) => getTypes(expr) ++ stats.flatMap:
        case t: Term => getTypes(t)
  // case _ => Nil

  private def foo[T: Type](using quotes: Quotes)(tree: quotes.reflect.Tree): Boolean =
    import quotes.reflect.*
    def matchesTargetType(tpe: TypeRepr): Boolean = tpe <:< TypeRepr.of[T]

    tree match
      case ValDef(_, tpt, Some(rhs)) => matchesTargetType(tpt.tpe) || foo[T](rhs)
      case DefDef(_, _, _, Some(rhs)) => foo[T](rhs)
      case Block(stats, expr) => stats.exists(foo[T]) || foo[T](expr)
      case Apply(fun, args) => foo[T](fun) || args.exists(foo[T])
      case TypeApply(fun, args) => foo[T](fun) || args.exists(e => matchesTargetType(e.tpe))
      case Typed(expr, tpt) => matchesTargetType(tpt.tpe) || foo[T](expr)
      case Inlined(_, bindings, expansion) => bindings.exists(foo[T]) || foo[T](expansion)
      case _ => false

  private def extractProgramBody(using quotes: Quotes)(programBody: quotes.reflect.Statement): quotes.reflect.Block =
    import quotes.reflect.*
    programBody match
        case Inlined(_, _, bodyBlock) => bodyBlock match
          case Block(applyBody :: Nil, _) => applyBody match
            case DefDef(_, _, _, Some(body)) => body match
              case Inlined(_, _, blockBody: Block) => blockBody
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
