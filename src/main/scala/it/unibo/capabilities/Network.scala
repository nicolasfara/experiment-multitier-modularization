package it.unibo.capabilities

import ox.Ox
import ox.flow.Flow

trait Network:
  def receiveFrom[V](from: String)(using Ox): V
  def receiveFlowFrom[V](from: String)(using Ox): Flow[V]
  def registerResult[V](produced: String, value: V): Unit
  def registerFlowResult[V](produced: String, value: Flow[V]): Unit
