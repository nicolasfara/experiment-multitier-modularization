package it.unibo.mode2.language

sealed trait Component[-Input <: Product, +Output]:
  type Capabilities
  def apply(inputs: Input): Output

trait LocalComponent[-Input <: Product, +Output] extends Component[Input, Output]

trait SharedData[+Data]:
  def local: Data

trait CollectiveComponent[-Input <: Product, Data <: SharedData[?]] extends Component[Input, Data]
