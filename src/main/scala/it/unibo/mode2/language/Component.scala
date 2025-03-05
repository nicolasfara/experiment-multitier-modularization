package it.unibo.mode2.language

sealed trait Component[-Input <: Product, +Output]:
  type Capabilities

trait LocalComponent[-Input <: Product, +Output] extends Component[Input, Output]:
  def apply(input: Input): Output

trait CollectiveComponent[-Input <: Product, +Output] extends Component[Input, Output]:
  def apply(input: Input): SharedData ?=> Output
