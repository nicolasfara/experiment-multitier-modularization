package it.unibo.capabilities

import io.circe.{Decoder, Encoder}
import it.unibo.capabilities.Multitier.{Placed, ResourceReference, at}
import it.unibo.capabilities.Multitier.Placed.*
import it.unibo.capabilities.Multitier.Placed.Quantifier.{Multiple, Single}
import ox.flow.Flow
import ox.{ExitCode, Ox, OxApp, never}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object PlacedMain:
  type Client <: { type Tie <: Single[Server] }
  type Server <: { type Tie <: Single[Client] }

  private inline def placedValueOn[P <: PlacedType](using Placed) = placed[P]:
    println("Generating a value into the Client")
    54

  private inline def processClientValueOnServer(using Placed)(input: Int at Client) = placed[Server]:
    val localValue = asLocal(input)
    println(s"Double $localValue on the Server")
    localValue * 2

  inline def myApp[P <: PlacedType](using PlacedAt[P]): Unit =
    val clientRes = placedValueOn[Client]
    val doubled = processClientValueOnServer(clientRes)
    placed[Client](println(s"Client received: ${asLocal(doubled)}"))

object PlacedAppClient extends OxApp:
  override def run(args: Vector[String])(using Ox): ExitCode =
    given clientWsNetwork: Network = WsNetwork(
      Map("it.unibo.capabilities.PlacedMain.Server" -> ("localhost", 8080)),
      Map(),
      port = 8081,
    )
    val clientRes = multitier[Unit, PlacedMain.Client](PlacedMain.myApp)
    println(clientRes)
    never

object PlacedAppServer extends OxApp:
  override def run(args: Vector[String])(using Ox): ExitCode =
    given serverWsNetwork: Network = WsNetwork(
      Map("it.unibo.capabilities.PlacedMain.Client" -> ("localhost", 8081)),
      Map(),
      port = 8080,
    )
    val serverRes = multitier[Unit, PlacedMain.Server](PlacedMain.myApp)
    println(serverRes)
    never

