package it.unibo.mode2.platform

import it.unibo.mode2.language.deployment.Application
import it.unibo.mode2.model.CollectiveData
import it.unibo.mode2.model.component.CollectiveComponent

trait Context:
  type DeviceId
  def collectiveDataFor[Data, C <: CollectiveComponent[?, Data]](component: C): CollectiveData[DeviceId, Data]

object Context:
  /** A device context identified by an [[Application]] [[id]] given a [[platform]] managing it.
    */
  def apply[Id](id: Id)(using platform: Platform): Context = new Context:
    type DeviceId = Id
    def collectiveDataFor[Data, C <: CollectiveComponent[?, Data]](component: C): CollectiveData[Id, Data] = ???
