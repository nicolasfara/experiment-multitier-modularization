package it.unibo.capabilities

import it.unibo.capabilities.Placed.Quantifier.{Multiple, Single}
import it.unibo.capabilities.Placed.{PlacedType, TiedMultipleTo, TiedSingleTo}
import it.unibo.capabilities.TypeUtils.placedTypeRepr
import ox.Ox

import scala.annotation.implicitNotFound
import scala.collection.mutable
import scala.compiletime.erasedValue

@implicitNotFound("To execute a multitier application, the `multitier` function must provide the corresponding handler")
class Placed(using Ox):
  self: Network =>
  type LocalPlace <: PlacedType

  private val multitierCall = mutable.Map[String, Int]()

  private enum PlacedValue[+V, +P <: PlacedType]:
    case Remote(resourceReference: String)
    case Local(value: V, resourceReference: String)

  infix opaque type at[+V, P <: PlacedType] = PlacedValue[V, P]

  class Locally[+LP]

  def asLocal[V, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](placed: V at Remote)(using Locally[Local]): V =
    import PlacedValue.*
    placed match
      case Remote(resourceReference) => receiveFrom(resourceReference) // Something with network call
      case Local(value, _)           => value

  def asLocalAll[V, Remote <: PlacedType, Local <: TiedMultipleTo[Remote]](placed: V at Remote)(using
      Locally[Local]
  ): Seq[V] =
    import PlacedValue.*
    placed match
      case Remote(resourceReference) => ???
      case Local(value, _)           => Seq(value)

  class PlaceContext[P <: PlacedType](using Locally[P]):
    inline def apply[V](inline body: Locally[P] ?=> V): V at P =
      import PlacedValue.*
      val typeRepr = placedTypeRepr[P]
      val count = multitierCall.getOrElse(typeRepr, 0)
      multitierCall(typeRepr) = count + 1
      inline erasedValue[P] match
        case _: LocalPlace => Local(body, s"$typeRepr:$count")
        case _             => Remote(s"$typeRepr:$count")

  def placed[P <: PlacedType]: PlaceContext[P] =
    PlaceContext[P](using Locally[P]())

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

  def asLocal[V, Remote <: PlacedType, Local <: TiedSingleTo[Remote]](using
      p: Placed,
      @implicitNotFound("Trying to access to a placed value from a peer not tied to the local one")
      u: p.Locally[Local]
  )(place: p.at[V, Remote]): V = p.asLocal(place)

  def asLocalAll[V, Remote <: PlacedType, Local <: TiedMultipleTo[Remote]](using
      p: Placed,
      @implicitNotFound("Trying to access to a placed value from multiple peers not tied to the local one")
      u: p.Locally[Local]
  )(place: p.at[V, Remote]): Seq[V] = p.asLocalAll(place)

  private class PlacedNetwork[P <: PlacedType](network: Network)(using Ox) extends Placed, Network:
    override type LocalPlace = P
    override def receiveFrom[V](from: String)(using Ox): V = network.receiveFrom(from)
    override def registerResult[V](produced: String, value: V): Unit = network.registerResult(produced, value)

  def multitier[V, P <: PlacedType](net: Network)(application: PlacedAt[P] ?=> V)(using Ox): V =
    given PlacedNetwork[P] = PlacedNetwork[P](net)
    application
