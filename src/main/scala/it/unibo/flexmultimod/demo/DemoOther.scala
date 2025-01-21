package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.core.language.Aggregate
import it.unibo.flexmultimod.core.{Component, Peer}

trait WithGp
trait WithAcc
trait WithNotifications
trait WithStorage
trait WithComputationalPower

type Smartphone = Peer & WithGp & WithAcc & WithNotifications
type Wearable = Peer & WithAcc & WithNotifications
type Edge = Peer & WithStorage & WithComputationalPower
type Cloud = Peer & WithComputationalPower

infix class TiedWith[A <: Peer, B <: Peer]
def deriveTie[A <: Peer, B <: Peer]: TiedWith[A, B] = TiedWith()

object WalkDetection1 extends Component[EmptyTuple, Double]:
  override type RequiredCapabilities = WithGps & WithAccelerometer
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmptyTuple): Double = ???

object HeartbeatSensing1 extends Component[EmptyTuple, Int]:
  override type RequiredCapabilities = WithAccelerometer
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmptyTuple): Int = ???

/* Collective component requiring the interaction with neighbor devices executing the same component instance.
 * Note the Aggregate[Boolean] return type, which represents the collective nature of the component.
 * This kind of components have an implicit "collective" input coming from the same component instance on neighbor devices.
 */
final case class EmergencyInput1(walking: Double, heartbeat: Int)
object CollectiveEmergency1 extends Component[EmergencyInput1, Aggregate[Boolean]]:
  override type RequiredCapabilities = Any
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmergencyInput1): Aggregate[Boolean] = ???

final case class AlertInput1(isEmergency: Boolean)
object ShowAlert1 extends Component[AlertInput, Unit]:
  override type RequiredCapabilities = WithNotification
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: AlertInput): Unit = ???

object Foo:
  given st: Smartphone TiedWith Wearable & Edge & Cloud = deriveTie
  given we: Wearable TiedWith Smartphone = deriveTie
  given ed: Edge TiedWith Cloud & Smartphone = deriveTie
  given cl: Cloud TiedWith Edge & Smartphone = deriveTie
