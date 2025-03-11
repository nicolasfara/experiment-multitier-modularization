package it.unibo.macs4s.model

import scala.collection.MapView

trait CollectiveData[Id, +Data]:
  val localId: Id
  val local: Data
  val neighborValues: MapView[Id, Data]

object CollectiveData:
  given [Id, D]: Conversion[CollectiveData[Id, D], D] with
    def apply(data: CollectiveData[Id, D]): D = data.local
