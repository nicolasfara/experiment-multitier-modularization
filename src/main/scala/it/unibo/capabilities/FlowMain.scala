package it.unibo.capabilities

import it.unibo.capabilities.Placed.Quantifier.{Multiple, Single}
import it.unibo.capabilities.Placed.{PlacedAt, PlacedType, asLocalFlow, placed}
import ox.flow.Flow
import ox.{ExitCode, Ox, OxApp}

import scala.concurrent.duration.*

object FlowMain extends OxApp:
  type Client <: { type Tie <: Single[Server] }
  type Server <: { type Tie <: Single[Client] }

  inline def eventFromClient(using p: Placed) = placed[Client].flowable:
    Flow.tick(1.second, "hello").take(10)

  inline def processClientEventsOnServer(using p: Placed)(input: p.flowAt[String, Client]) = placed[Server]:
    val collectedEvents = asLocalFlow(input)
    println(s"Collected from client: ${collectedEvents.runToList()}")

  inline def myFlowApp[P <: PlacedType](using PlacedAt[P]) =
    val clientEvents = eventFromClient
    processClientEventsOnServer(clientEvents)

  override def run(args: Vector[String])(using Ox): ExitCode = ???
