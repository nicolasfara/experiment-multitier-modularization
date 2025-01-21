package it.unibo.mode2.language

trait Component[-Input <: Product, +Output, RequiredCapabilities]:
  def apply(inputs: Input): Output
