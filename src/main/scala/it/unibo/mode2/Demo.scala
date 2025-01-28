package it.unibo.mode2

import it.unibo.mode2.language.Component

trait Foo
trait Bar
trait Baz


object MovementDetection extends Component[EmptyTuple, Double]:
  override type Capabilities >: Foo | Baz | Int | "Porca Madonna" | 1 | 4211412
  override def apply(input: EmptyTuple): Double = ???

trait DistanceBetween extends Component[EmptyTuple, Double]:
  override def apply(input: EmptyTuple): Double = ???
