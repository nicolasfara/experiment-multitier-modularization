package it.unibo.capabilities

import it.unibo.capabilities.Placed.{PlacedType, TiedTo}
import it.unibo.capabilities.TypeUtils.placedTypeRepr

import scala.collection.mutable
import scala.compiletime.erasedValue

trait Network

class Placed:
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
      case Remote(resourceReference) => ??? // Something with network call
      case Local(value, _)           => value

  class PlaceContext[P <: PlacedType]:
    inline def apply[V](body: Locally[P] ?=> V): V at P =
      import PlacedValue.*
      given Locally[P]()
      val typeRepr = placedTypeRepr[P]
      val occurrencyCount = multitierCall.getOrElse(typeRepr, 0)
      multitierCall(typeRepr) = occurrencyCount + 1
      inline erasedValue[P] match
        case _: LocalPlace => Local(body, s"$typeRepr:$occurrencyCount")
        case _             => Remote(s"$typeRepr:$occurrencyCount")

  def placed[P <: PlacedType]: PlaceContext[P] = PlaceContext[P]()

object Placed:
  type PlacedAt[Peer <: PlacedType] = Placed { type LocalPlace = Peer }
  type PlacedType = { type Tie }
  type TiedTo[P <: PlacedType] = { type Tie <: P }

  def placed[P <: PlacedType](using p: Placed): p.PlaceContext[P] =
    p.placed

  def asLocal[V, P <: PlacedType, Local <: TiedTo[P]](using
      p: Placed,
      u: p.Locally[Local]
  )(place: p.at[V, P]): V = p.asLocal(place)

  class PlacedNetwork[P <: PlacedType](network: Network) extends Placed, Network:
    override type LocalPlace = P

  def multitier[V, P <: PlacedType](net: Network)(application: PlacedAt[P] ?=> V): V =
    given PlacedNetwork[P] = PlacedNetwork[P](net)
    application

object MApp extends App:
  type Client <: { type Tie <: Server }
  type Server <: { type Tie <: Client }

  import Placed.*

  inline def placedValueOn[P <: PlacedType](using Placed) = placed[P]:
    42

  inline def processClientValueOnServer(using p: Placed)(input: p.at[Int, Client]) = placed[Server]:
    val localValue = asLocal(input)
    localValue * 2

  inline def myApp[P <: PlacedType](using PlacedAt[P]): Unit =
    val clientRes = placedValueOn[Client]
    val clientRes2 = placedValueOn[Client]
    println(s"clientRes: $clientRes")
    println(s"clientRes2: $clientRes2")
    val doubled = processClientValueOnServer(clientRes)
    println(s"doubled: $doubled")
    placed[Client](println(asLocal(doubled)))

  // ------------- Endpoint Projection
  private val network = new Network {}
  private val clientRes = multitier[Unit, Client](network)(myApp)
  println(clientRes)
//  private val serverRes = multitier[Unit, Server](network)(myApp)
//  println(serverRes)

//trait Placed:
//  type Placement
//  private case class PlacementRef[P](name: String)
//  private object PlacementRef:
//    def fromType[P]: PlacementRef[P] = ???
//  infix opaque type at[Value, P] = (Value, PlacementRef[P])
//
//  def placedOn[P, V](body: => V): V at P = (body, PlacementRef.fromType[P])
//
//  def capture[V, P1](placedValue: Placed#at[V, P1]): V
//
//object Placed:
//  def placedOn[P, V](body: => V)(using p: Placed { type Placement = P }): p.at[V, P] = p.placedOn(body)
//
//  def capture[V, P1](using p: Placed)(placedValue: Placed#at[V, P1]): V =
//    p.capture[V, P1](placedValue)
//
//  trait Tagged[P]
//
//  type PlacedAt[P] = Placed { type Placement = P }
//
//  given localPlace: [Pl] => Tagged[Pl] => PlacedAt[Pl] = new Placed:
//    override type Placement = Pl
//    override def capture[V, P1](placedValue: Placed#at[V, P1]): V = ???
//  given remotePlace: [Pl] => NotGiven[Tagged[Pl]] => PlacedAt[Pl] = new Placed:
//    override type Placement = Pl
//    override def capture[V, P1](placedValue: Placed#at[V, P1]): V = ???
//
//object Foo extends App:
//  type Client <: { type Tie <: Server }
//  type Server <: { type Tie <: Client }
//
//  import Placed.*
//
//  def foo(using PlacedAt[Client]) = placedOn:
//    12.0
//
//  def bar(input: Double)(using PlacedAt[Server]) = placedOn:
//    println(input)
//    ()
//
//  def program(using PlacedAt[Client]) = placedOn[Client, Unit]:
//    val fooRes = foo
//    placedOn[Server, Unit]:
//      val clientResult = capture(fooRes)
//      bar(clientResult)
//    ()
//
//  given Tagged[Client]
//  program

//-------------------------------------------------------------------------------------------------

//import it.unibo.capabilities.Language.{at, on}

//type Multitier[R] = MultitierLanguage ?=> R
//
//trait MultitierLanguage:
//  infix type at[Value, Place] = Place ?=> Value
//
//  extension [Value, Place](placed: Value at Place) def asLocal: Value = ???
//
//  def on[Value, Place](body: Place ?=> Value): Value at Place
//
//  def capture[Value, P1, P2](placedValue: Value at P1): Value at P2
//
//object MultitierLanguage:
//  def on[Value, Place](body: Place ?=> Value)(using mtl: MultitierLanguage): Value at Place =
//    mtl.on[Value, Place](body)
//
//  def capture[Value, P1, P2](placedValue: Value at P1)(using mtl: MultitierLanguage): Value at P2 =
//    mtl.capture[Value, P1, P2](placedValue)
//
//object Client:
//  type Tie <: Server.type
//
//object Server:
//  type Tie <: Client.type
//
//import MultitierLanguage.*
//
//def myFunction(inputs: Int): Multitier[Int on Client.type] = on[Int, Client.type]:
//  12
