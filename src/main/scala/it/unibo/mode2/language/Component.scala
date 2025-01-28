package it.unibo.mode2.language

trait Component[-Input <: Product, +Output]:
  type Capabilities
  def apply(inputs: Input): Output
