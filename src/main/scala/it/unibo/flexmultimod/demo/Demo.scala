package it.unibo.flexmultimod.demo

import it.unibo.flexmultimod.core.Cardinality.*
import it.unibo.flexmultimod.core.{ApplicationPeer, Component, InfrastructuralPeer, Peer}
import it.unibo.flexmultimod.core.language.{Aggregate, Language, Macroprogram}
import it.unibo.flexmultimod.platform.Platform
import it.unibo.flexmultimod.core.language.Language.*

trait WithAi
trait WithGps
trait WithAccelerometer
trait WithNotification

object WalkDetection extends Component[EmptyTuple, Double]:
  override type RequiredCapabilities = WithGps & WithAccelerometer
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmptyTuple): Double = ???

object HeartbeatSensing extends Component[EmptyTuple, Int]:
  override type RequiredCapabilities = WithAccelerometer
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmptyTuple): Int = ???

/* Collective component requiring the interaction with neighbor devices executing the same component instance.
 * Note the Aggregate[Boolean] return type, which represents the collective nature of the component.
 * This kind of components have an implicit "collective" input coming from the same component instance on neighbor devices.
 */
final case class EmergencyInput(walking: Double, heartbeat: Int)
object CollectiveEmergency extends Component[EmergencyInput, Aggregate[Boolean]]:
  override type RequiredCapabilities = Any
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: EmergencyInput): Aggregate[Boolean] = ???

final case class AlertInput(isEmergency: Boolean)
object ShowAlert extends Component[AlertInput, Unit]:
  override type RequiredCapabilities = WithNotification
  override def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: AlertInput): Unit = ???

/*
 * 1. Specify differently the logical network (?) -- another type-based specification?
 * 2. Intercept invalid components "wiring" at compile time? Offloading to a device not directly connected to the current device.
 * 3. The "Tie" definition defines a one-way connection between two peers types.
 *    A corresponding "Tie" definition is needed for the other direction.
 *    - A Tie B means that A can send messages to B, but B cannot send messages to A.
 *    - [A Tie B] & [B Tie A] means a bidirectional connection.
 * 4. Code between components: where it will be executed? (Single code specification | macroprogram specification)
 *    - On the ApplicationPeer would be the best choice.
 *    - On all the devices? Possible useless...
 * 4. Scafi3 Integration?
 * 5. What about reconfiguration?
 */
object MacroApp:
  type Smartphone <: ApplicationPeer & WithGps & WithAccelerometer & WithNotification:
    type Tie <: Single[Cloud] & Single[Wearable]
  type Wearable <: InfrastructuralPeer & WithAccelerometer:
    type Tie <: Single[Smartphone]
  type Cloud <: InfrastructuralPeer & WithAi:
    type Tie <: Multiple[Cloud] & Multiple[Smartphone]

  def macroProgram[Placement <: Peer]: Macroprogram =
    program[Placement, Unit]:
      val walking = WalkDetection[Smartphone](EmptyTuple)
      val heartBeat = HeartbeatSensing[Wearable](EmptyTuple)
      val emergency = CollectiveEmergency[Cloud](EmergencyInput(walking, heartBeat)).localValue
      ShowAlert[Smartphone](AlertInput(emergency))
