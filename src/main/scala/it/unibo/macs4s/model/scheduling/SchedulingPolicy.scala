package it.unibo.macs4s.model.scheduling

import scala.concurrent.duration.FiniteDuration
import scala.annotation.tailrec

import it.unibo.macs4s.platform.Context

import gears.async.*
import gears.async.AsyncOperations.sleep
import gears.async.default.given

trait SchedulingPolicy:
  def schedule[Outcome](computation: => Outcome)(using Async, Context): Outcome

trait Periodic(val interval: FiniteDuration) extends SchedulingPolicy:
  @tailrec
  final override def schedule[Outcome](computation: => Outcome)(using Async, Context): Outcome =
    computation
    sleep(interval)
    schedule(computation)

object Periodic:
  def apply(interval: FiniteDuration): Periodic = new Periodic(interval) {}

trait DeferredScheduling extends SchedulingPolicy:
  def schedulingPolicy(using Context): SchedulingPolicy

  final override def schedule[Outcome](computation: => Outcome)(using Async, Context): Outcome =
    schedulingPolicy.schedule(computation)
