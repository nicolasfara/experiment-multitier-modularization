package it.unibo.capabilities

import it.unibo.capabilities.Multitier.Placed.PlacedType

import scala.quoted.{Expr, Quotes, Type}

object TypeUtils:
  inline def placedTypeRepr[P <: PlacedType]: String = ${ placedTypeReprImpl[P] }

  private def placedTypeReprImpl[P : Type](using quotes: Quotes): Expr[String] =
    import quotes.reflect.*
    val re = TypeRepr.of[P].show
    Expr(re)
