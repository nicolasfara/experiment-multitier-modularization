package it.unibo.capabilities

import it.unibo.capabilities.Multitier.Placed.Quantifier.{Multiple, Single}
import it.unibo.capabilities.Multitier.Placed.{PlacedType, TiedMultipleTo, TiedSingleTo}
import it.unibo.capabilities.TypeUtils.placedTypeRepr
import ox.Ox
import ox.flow.Flow

import scala.annotation.{implicitNotFound, targetName}
import scala.collection.mutable
import scala.compiletime.erasedValue

object Multitier:
  infix opaque type at[+V, P <: PlacedType] = PlacedValue[V, P]
  infix opaque type flowAt[+V, P <: PlacedType] = PlacedValue[Flow[V], P]

  private enum PlacedValue[+V, +P <: PlacedType]:
    case Remote(resourceReference: String)
    case Local(value: V, resourceReference: String)

//  class Whatever[T, O] extends (T => O)
//
//  object Whatever:
//    inline given [T, P <: PlacedType]: Whatever[T, T at P] with
//      override def apply(body: T): T at P = ???
//
//    inline given [T, P <: PlacedType]: Whatever[T, T flowAt P] with
//      override def apply(body: Flow[T]): T flowAt P = ???

  @implicitNotFound(
    "To execute a multitier application, the `multitier` function must provide the corresponding handler"
  )
  class Placed(using Ox):
    self: Network =>
    type LocalPlace <: PlacedType

    private val multitierCall = mutable.Map[String, Int]()

    class PlacementScope[+LP]

    def asLocalFlow[V, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](placedFlow: V flowAt Remote)(using
        PlacementScope[Local]
    ): Flow[V] =
      import PlacedValue.*
      placedFlow match
        case Remote(resourceReference) => receiveFlowFrom(resourceReference)
        case Local(value, _)           => value

    def asLocal[V, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](placed: V at Remote)(using
        PlacementScope[Local]
    ): V =
      import PlacedValue.*
      placed match
        case Remote(resourceReference) => receiveFrom(resourceReference) // Something with network call
        case Local(value, _)           => value

    def asLocalAll[V, Remote <: PlacedType, Local <: TiedMultipleTo[Remote]](placed: V at Remote)(using
        PlacementScope[Local]
    ): Seq[V] =
      import PlacedValue.*
      placed match
        case Remote(resourceReference) => ???
        case Local(value, _)           => Seq(value)

    class PlaceContext[P <: PlacedType](using PlacementScope[P]):
      inline def flowable[V](inline body: PlacementScope[P] ?=> Flow[V]): V flowAt P = applyable(body, true)
      inline def apply[V](inline body: PlacementScope[P] ?=> V): V at P = applyable(body, false)
      private inline def applyable[V, O <: PlacedType](
          body: PlacementScope[P] ?=> V,
          isFlow: Boolean
      ): PlacedValue[V, O] =
        import PlacedValue.*
        val typeRepr = placedTypeRepr[P]
        val count = multitierCall.getOrElse(typeRepr, 0)
        multitierCall(typeRepr) = count + 1
        val strRepr = s"$typeRepr:${if isFlow then "flow:" else ""}$count"
        inline erasedValue[P] match
          case _: LocalPlace => Local(body, strRepr)
          case _             => Remote(strRepr)

    def placed[P <: PlacedType]: PlaceContext[P] =
      PlaceContext[P](using PlacementScope[P]())

  object Placed:
    type PlacedType = { type Tie }
    type PlacedAt[Peer <: PlacedType] = Placed { type LocalPlace = Peer }
    type TiedSingleTo[+P <: PlacedType] = { type Tie <: Single[P] }
    type TiedMultipleTo[+P <: PlacedType] = { type Tie <: Multiple[P] }

    enum Quantifier[+P <: PlacedType]:
      case Single()
      case Multiple()

    def placed[P <: PlacedType](using p: Placed): p.PlaceContext[P] =
      p.placed

    def asLocalFlow[V, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](using
        p: Placed,
        l: p.PlacementScope[Local]
    )(placedFlow: V flowAt Remote): Flow[V] = p.asLocalFlow(placedFlow)

    def asLocal[V, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](using
        p: Placed,
        @implicitNotFound("Trying to access to a placed value from a peer not tied to the local one")
        u: p.PlacementScope[Local]
    )(place: V at Remote): V = p.asLocal(place)

    def asLocalAll[V, Remote <: PlacedType, Local <: TiedMultipleTo[Remote]](using
        p: Placed,
        @implicitNotFound("Trying to access to a placed value from multiple peers not tied to the local one")
        u: p.PlacementScope[Local]
    )(place: V at Remote): Seq[V] = p.asLocalAll(place)

    private class PlacedNetwork[P <: PlacedType](network: Network)(using Ox) extends Placed, Network:
      override type LocalPlace = P
      override def receiveFrom[V](from: String)(using Ox): V = network.receiveFrom(from)
      override def registerResult[V](produced: String, value: V): Unit = network.registerResult(produced, value)
      override def receiveFlowFrom[V](from: String)(using Ox): Flow[V] = network.receiveFlowFrom(from)
      override def registerFlowResult[V](produced: String, value: Flow[V]): Unit =
        network.registerFlowResult(produced, value)

    def multitier[V, P <: PlacedType](net: Network)(application: PlacedAt[P] ?=> V)(using Ox): V =
      given PlacedNetwork[P] = PlacedNetwork[P](net)
      application
