package it.unibo.capabilities

import io.circe.{Decoder, Encoder}
import it.unibo.capabilities.Multitier.{Placed, ResourceReference, at}
import it.unibo.capabilities.Multitier.Placed.*
import it.unibo.capabilities.Multitier.Placed.Quantifier.{Multiple, Single}
import ox.flow.Flow
import ox.{ExitCode, Ox, OxApp, sleep}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object PlacedMain extends OxApp:
  type Client <: { type Tie <: Single[Server] }
  type Server <: { type Tie <: Multiple[Client] }

  private inline def placedValueOn[P <: PlacedType](using Placed) = placed[P]:
    println("Generating a value into the Client")
    42

  private inline def processClientValueOnServer(using Placed)(input: Int at Client) = placed[Server]:
    val localValue = asLocalAll(input)
    println(s"Double $localValue on the Server")
    localValue.sum * 2

  private inline def myApp[P <: PlacedType](using PlacedAt[P]): Unit =
    val clientRes = placedValueOn[Client]
    val doubled = processClientValueOnServer(clientRes)
    placed[Client](println(s"Client received: ${asLocal(doubled)}"))

  override def run(args: Vector[String])(using Ox): ExitCode =
    val fakeClientNetwork = new Network:
      private val outbound = mutable.Map[String, Any]()
      private val inbound = mutable.Map("it.unibo.capabilities.PlacedMain.Server:0" -> 84)
      override def receiveFrom[V: Decoder](from: ResourceReference)(using Ox): V =
        sleep(2.seconds)
        inbound(s"${from.peerName}:${from.index}").asInstanceOf[V]
      override def registerResult[V: Encoder](produced: ResourceReference, value: V): Unit =
        outbound(s"${produced.peerName}:${produced.index}") = value
      override def receiveFlowFrom[V: Decoder](from: ResourceReference)(using Ox): Flow[V] = ???
      override def registerFlowResult[V: Encoder](produced: ResourceReference, value: Flow[V]): Unit = ???
      override def receiveFromAll[V: Decoder](from: ResourceReference)(using Ox): Seq[V] = ???

    val fakeServerNetwork = new Network:
      private val outbound = mutable.Map[String, Any]()
      private val inbound = mutable.Map("it.unibo.capabilities.PlacedMain.Client:0" -> 42)
      override def receiveFrom[V: Decoder](from: ResourceReference)(using Ox): V =
        sleep(2.seconds)
        inbound(s"${from.peerName}:${from.index}").asInstanceOf[V]
      override def registerResult[V: Encoder](produced: ResourceReference, value: V): Unit =
        outbound(s"${produced.peerName}:${produced.index}") = value
      override def receiveFlowFrom[V: Decoder](from: ResourceReference)(using Ox): Flow[V] = ???
      override def registerFlowResult[V: Encoder](produced: ResourceReference, value: Flow[V]): Unit = ???
      override def receiveFromAll[V: Decoder](from: ResourceReference)(using Ox): Seq[V] = ???

    val clientRes = multitier[Unit, Client](fakeClientNetwork)(myApp)
    println(clientRes)
//    val serverRes = multitier[Unit, Server](fakeServerNetwork)(myApp)
//    println(serverRes)
    ExitCode.Success
