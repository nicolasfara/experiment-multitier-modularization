package it.unibo.mode2.language.scheduling

import gears.async.*
import gears.async.AsyncOperations.sleep
import gears.async.default.given
import it.unibo.mode2.language.component.Component

import scala.concurrent.duration.FiniteDuration
import it.unibo.mode2.platform.Context

import scala.annotation.tailrec

trait SchedulingPolicy:
  def schedule(component: Component[?, ?])(using Async, Context): Unit

trait Periodic(val interval: FiniteDuration) extends SchedulingPolicy:
  @tailrec
  final override def schedule(component: Component[?, ?])(using async: Async, ctx: Context): Unit =
    // ctx.prepareRoundFor(component)
    sleep(interval)
    schedule(component)

object Periodic:
  def apply(interval: FiniteDuration): Periodic = new Periodic(interval) { }

trait DependentScheduling extends SchedulingPolicy:
  def schedulingPolicy(using Context): SchedulingPolicy

  final override def schedule(component: Component[?, ?])(using Async, Context): Unit = ???
