package it.unibo.capabilities

import ox.{Ox, fork}

trait Network:
  def receiveFrom[V](from: String)(using Ox): V
  def registerResult[V](produced: String, value: V): Unit
