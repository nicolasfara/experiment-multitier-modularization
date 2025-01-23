package it.unibo.mode2.language

sealed trait Device[ID: Ordering, Capabilities]
trait Application[ID, Capabilities] extends Device[ID, Capabilities]
trait Infrastructural[ID, Capabilities] extends Device[ID, Capabilities]

trait Smartphone[ID] extends Application[ID, Any]
trait Wearable[ID] extends Infrastructural[ID, Any]

class Smartphone1 extends Smartphone[1]
object Smartphone2 extends Smartphone[2]
object Wearable1 extends Wearable[1]
