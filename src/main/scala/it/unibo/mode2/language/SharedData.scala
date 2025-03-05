package it.unibo.mode2.language

trait SharedData:
  def local[Data, C <: CollectiveComponent[?, Data]](component: C): Data
