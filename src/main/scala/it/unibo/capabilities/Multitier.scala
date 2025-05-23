package it.unibo.capabilities

import io.circe.{Decoder, Encoder}
import it.unibo.capabilities.Multitier.Placed.Quantifier.{Multiple, Single}
import it.unibo.capabilities.Multitier.Placed.{PlacedType, TiedMultipleTo, TiedSingleTo}
import it.unibo.capabilities.TypeUtils.placedTypeRepr
import ox.{Ox, supervised}
import ox.flow.Flow

import scala.annotation.{implicitNotFound, targetName}
import scala.collection.mutable
import scala.compiletime.erasedValue
import scala.reflect.ClassTag

object Multitier:
  export io.circe.generic.auto.*

  infix opaque type at[+V, P <: PlacedType] = PlacedValue[V, P]
  infix opaque type flowAt[+V, P <: PlacedType] = PlacedValue[Flow[V], P]

  enum ValueType:
    case Flow
    case Value

  final case class ResourceReference(peerName: String, index: Int, valueType: ValueType):
    override def toString: String = s"$peerName@$index[$valueType]"

  private enum PlacedValue[+V, +P <: PlacedType]:
    case Remote(resourceReference: ResourceReference)
    case Local(value: V, resourceReference: ResourceReference)

  @implicitNotFound(
    "To execute a multitier application, the `multitier` function must provide the corresponding handler"
  )
  trait Placed(using Network):
    type LocalPlace <: PlacedType

    private val multitierCall = scala.collection.concurrent.TrieMap[String, Int]()
    private val multitierValueCall = scala.collection.concurrent.TrieMap[String, Int]()

    class PlacementScope[+LP]

    def asLocalFlow[V: Decoder, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](placedFlow: V flowAt Remote)(using
        PlacementScope[Local],
        Ox
    ): Flow[V] =
      import PlacedValue.*
      placedFlow match
        case Remote(resourceReference) => summon[Network].receiveFlowFrom(resourceReference)
        case Local(value, _)           => value

    def asLocal[V: Decoder, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](placed: V at Remote)(using
        PlacementScope[Local],
        Ox
    ): V =
      import PlacedValue.*
      placed match
        case Remote(resourceReference) => summon[Network].receiveFrom(resourceReference)
        case Local(value, _)           => value

    def local[V: Decoder, Local <: PlacedType](placed: V at Local)(using
        PlacementScope[Local]
    ): V =
      import PlacedValue.*
      placed match
        case Remote(_)       => throw new Exception("The value must be local but it is not")
        case Local(value, _) => value

    def asLocalAll[V: Decoder, Remote <: PlacedType, Local <: TiedMultipleTo[Remote]](placed: V at Remote)(using
        PlacementScope[Local],
        Ox
    ): Seq[V] =
      import PlacedValue.*
      placed match
        case Remote(resourceReference) => summon[Network].receiveFromAll(resourceReference)
        case Local(value, _)           => Seq(value)

    class PlaceContext[P <: PlacedType](send: Boolean = true)(using p: PlacementScope[P], ox: Ox):
      inline def flowable[V](inline body: PlacementScope[P] ?=> Flow[V]): V flowAt P = ??? // applyable(body, false)
      inline def apply[V: Encoder](inline body: PlacementScope[P] ?=> V): V at P = applyable(body, false)
      private inline def applyable[V: Encoder, O <: PlacedType](
          inline body: PlacementScope[P] ?=> V,
          isFlow: Boolean
      ): PlacedValue[V, O] =
        import PlacedValue.*
        val typeRepr = placedTypeRepr[P]
        val count = multitierCall.getOrElse(typeRepr, 0)
        multitierCall(typeRepr) = count + 1
        val resReference = ResourceReference(typeRepr, count, if isFlow then ValueType.Flow else ValueType.Value)
        inline erasedValue[P] match
          case _: LocalPlace =>
            val res = body
            if send then summon[Network].registerResult(resReference, res)
            Local(res, resReference)
          case _ => Remote(resReference)

    class ValueContext[P <: PlacedType](using p: PlacementScope[P]):
      inline def apply[V: Encoder](inline body: PlacementScope[P] ?=> V): V at P =
        import PlacedValue.*
        val typeRepr = placedTypeRepr[P]
        val count = multitierValueCall.getOrElse(typeRepr, 0)
        multitierValueCall(typeRepr) = count + 1
        val resReference = ResourceReference(typeRepr, count, ValueType.Value)
        inline erasedValue[P] match
          case _: LocalPlace => Local(body, resReference)
          case _ => Remote(resReference)

    inline def placed[P <: PlacedType](using Ox): PlaceContext[P] =
      PlaceContext[P](true)(using PlacementScope[P]())

    inline def on[P <: PlacedType]: ValueContext[P] =
      ValueContext[P](using PlacementScope[P]())

  object Placed:
    type PlacedType = { type Tie }
    type PlacedAt[Peer <: PlacedType] = Placed { type LocalPlace = Peer }
    type TiedSingleTo[+P <: PlacedType] = { type Tie <: Single[P] }
    type TiedMultipleTo[+P <: PlacedType] = { type Tie <: Multiple[P] }

    enum Quantifier[+P <: PlacedType]:
      case Single()
      case Multiple()

    inline def placed[P <: PlacedType](using p: Placed, ox: Ox): p.PlaceContext[P] =
      p.placed

    inline def on[P <: PlacedType](using p: Placed): p.ValueContext[P] =
      p.on

    def asLocalFlow[V: Decoder, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](using
        p: Placed,
        l: p.PlacementScope[Local],
        ox: Ox
    )(placedFlow: V flowAt Remote): Flow[V] = p.asLocalFlow(placedFlow)

    def asLocal[V: Decoder, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](using
        p: Placed,
        @implicitNotFound("Trying to access to a placed value from a peer not tied to the local one")
        u: p.PlacementScope[Local],
        ox: Ox
    )(place: V at Remote): V = p.asLocal(place)

    def local[V: Decoder, Local <: PlacedType](using p: Placed, u: p.PlacementScope[Local])(place: V at Local): V =
      p.local(place)

    def asLocalAll[V: Decoder, Remote <: PlacedType, Local <: TiedMultipleTo[Remote]](using
        p: Placed,
        @implicitNotFound("Trying to access to a placed value from multiple peers not tied to the local one")
        u: p.PlacementScope[Local],
        ox: Ox
    )(place: V at Remote): Seq[V] = p.asLocalAll(place)

    private class PlacedNetwork[P <: PlacedType](using Ox, Network) extends Placed:
      override type LocalPlace = P

    def multitier[V, P <: PlacedType](application: PlacedAt[P] ?=> V)(using Ox, Network): V =
      given PlacedNetwork[P] = PlacedNetwork[P]()
      summon[Network].startNetwork
      application
