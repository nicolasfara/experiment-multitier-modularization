package it.unibo.capabilities

import it.unibo.capabilities.Multitier.{Placed, flowAt}
import it.unibo.capabilities.Multitier.Placed.Quantifier.Single
import it.unibo.capabilities.Multitier.Placed.{PlacedAt, PlacedType, asLocalFlow, placed}
import ox.flow.Flow
import ox.{ExitCode, Ox, OxApp}

import scala.concurrent.duration.*

object FlowMain extends OxApp:
  type Client <: { type Tie <: Single[Server] }
  type Server <: { type Tie <: Single[Client] }

  inline def eventFromClient(using Placed) = placed[Client].flowable:
    Flow.tick(1.second, "hello").take(10)

  inline def processClientEventsOnServer(using Placed)(input: String flowAt Client) = placed[Server]:
    val collectedEvents = asLocalFlow(input)
    println(s"Collected from client: ${collectedEvents.runToList()}")

  inline def myFlowApp[P <: PlacedType](using PlacedAt[P]) =
    val clientEvents = eventFromClient
    processClientEventsOnServer(clientEvents)

  override def run(args: Vector[String])(using Ox): ExitCode = ???
