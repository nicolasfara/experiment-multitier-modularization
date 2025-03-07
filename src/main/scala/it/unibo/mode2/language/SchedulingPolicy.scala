package it.unibo.mode2.language

import scala.concurrent.duration.FiniteDuration

trait SchedulingPolicy

trait Periodic(val interval: FiniteDuration) extends SchedulingPolicy
