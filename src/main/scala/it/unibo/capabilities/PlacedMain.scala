package it.unibo.capabilities

import ox.{ExitCode, Ox, OxApp, sleep}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object PlacedMain extends OxApp:
  type Client <: { type Tie <: Server }
  type Server <: { type Tie <: Client }

  import Placed.*

  inline def placedValueOn[P <: PlacedType](using Placed) = placed[P]:
    println("Generating a value into the Client")
    42

  inline def processClientValueOnServer(using p: Placed)(input: p.at[Int, Client]) = placed[Server]:
    val localValue = asLocal(input)
    println(s"Double $localValue on the Server")
    localValue * 2

  inline def myApp[P <: PlacedType](using PlacedAt[P]): Unit =
    val clientRes = placedValueOn[Client]
    val doubled = processClientValueOnServer(clientRes)
    placed[Client](println(s"Client received: ${asLocal(doubled)}"))

  override def run(args: Vector[String])(using Ox): ExitCode =
    val fakeClientNetwork = new Network:
      private val outbound = mutable.Map[String, Any]()
      private val inbound = mutable.Map("it.unibo.capabilities.PlacedMain.Server:0" -> 84)
      override def receiveFrom[V](from: String)(using Ox): V =
        sleep(2.seconds)
        inbound(from).asInstanceOf[V]
      override def registerResult[V](produced: String, value: V): Unit = outbound(produced) = value

    val fakeServerNetwork = new Network:
      private val outbound = mutable.Map[String, Any]()
      private val inbound = mutable.Map("it.unibo.capabilities.PlacedMain.Client:0" -> 42)
      override def receiveFrom[V](from: String)(using Ox): V =
        sleep(2.seconds)
        inbound(from).asInstanceOf[V]
      override def registerResult[V](produced: String, value: V): Unit = outbound(produced) = value

    val clientRes = multitier[Unit, Client](fakeClientNetwork)(myApp)
    println(clientRes)
//    val serverRes = multitier[Unit, Server](fakeServerNetwork)(myApp)
//    println(serverRes)
    ExitCode.Success
