package it.unibo.capabilities

import it.unibo.capabilities.Placed.{PlacedType, TiedTo}
import it.unibo.capabilities.TypeUtils.placedTypeRepr
import ox.{Ox, supervised}

import scala.collection.mutable
import scala.compiletime.erasedValue

class Placed(using Ox):
  self: Network =>
  type LocalPlace <: PlacedType

  private val multitierCall = mutable.Map[String, Int]()

  private enum PlacedValue[+V, +P <: PlacedType]:
    case Remote(resourceReference: String)
    case Local(value: V, resourceReference: String)

  infix opaque type at[+V, P <: PlacedType] = PlacedValue[V, P]

  class Locally[LP]

  def asLocal[V, P <: PlacedType, Local <: TiedTo[P]](placed: V at P)(using l: Locally[Local]): V =
    import PlacedValue.*
    placed match
      case Remote(resourceReference) => receiveFrom(resourceReference) // Something with network call
      case Local(value, _)           => value

  class PlaceContext[P <: PlacedType]:
    inline def apply[V](body: Locally[P] ?=> V): V at P =
      import PlacedValue.*
      given Locally[P]()
      val typeRepr = placedTypeRepr[P]
      val count = multitierCall.getOrElse(typeRepr, 0)
      multitierCall(typeRepr) = count + 1
      inline erasedValue[P] match
        case _: LocalPlace => Local(body, s"$typeRepr:$count")
        case _             => Remote(s"$typeRepr:$count")

  def placed[P <: PlacedType]: PlaceContext[P] = PlaceContext[P]()

object Placed:
  type PlacedAt[Peer <: PlacedType] = Placed { type LocalPlace = Peer }
  type PlacedType = { type Tie }
  type TiedTo[P <: PlacedType] = { type Tie <: P }

  def placed[P <: PlacedType](using p: Placed): p.PlaceContext[P] =
    p.placed

  def asLocal[V, P <: PlacedType, Local <: TiedTo[P]](using
      p: Placed,
      u: p.Locally[Local],
  )(place: p.at[V, P]): V = p.asLocal(place)

  private class PlacedNetwork[P <: PlacedType](network: Network)(using Ox) extends Placed, Network:
    override type LocalPlace = P
    override def receiveFrom[V](from: String)(using Ox): V = network.receiveFrom(from)
    override def registerResult[V](produced: String, value: V): Unit = network.registerResult(produced, value)

  def multitier[V, P <: PlacedType](net: Network)(application: PlacedAt[P] ?=> V)(using Ox): V =
    given PlacedNetwork[P] = PlacedNetwork[P](net)
    application
