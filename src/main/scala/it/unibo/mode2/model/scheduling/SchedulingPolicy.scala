package it.unibo.mode2.model.scheduling

import gears.async.*
import gears.async.AsyncOperations.sleep
import gears.async.default.given

import scala.concurrent.duration.FiniteDuration
import it.unibo.mode2.platform.Context

import scala.annotation.tailrec

trait SchedulingPolicy:
  def schedule[Outcome](computation: => Outcome)(using Async, Context): Outcome

trait Periodic(val interval: FiniteDuration) extends SchedulingPolicy:
  @tailrec
  final override def schedule[Outcome](computation: => Outcome)(using Async, Context): Outcome =
    computation
    sleep(interval)
    schedule(computation)

object Periodic:
  def apply(interval: FiniteDuration): Periodic = new Periodic(interval) { }

trait DeferredScheduling extends SchedulingPolicy:
  def schedulingPolicy(using Context): SchedulingPolicy

  final override def schedule[Outcome](computation: => Outcome)(using Async, Context): Outcome =
    schedulingPolicy.schedule(computation)
