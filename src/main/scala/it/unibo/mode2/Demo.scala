package it.unibo.mode2

import it.unibo.mode2.language.Component

object MovementDetection extends Component[EmptyTuple, Double, Int]:
  override def apply(input: EmptyTuple): Double = ???

object DistanceBetween extends Component[EmptyTuple, Double, Nothing]:
  override def apply(input: EmptyTuple): Double = ???
