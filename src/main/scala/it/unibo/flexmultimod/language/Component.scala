package it.unibo.flexmultimod.language

import it.unibo.flexmultimod.language.FlexMultiModLanguage.on
import it.unibo.flexmultimod.tier.Peer

trait Aggregate

/** Generic movable computation.
  */
sealed trait Component[-Inputs <: Tuple, +Output]:
  /** The constraints required by the [[Component]] to be executed.
    */
  type Constraints

/** Module executed locally to the [[PlacedPeer]].
  *
  * This kind of module do not require any "neighbor interaction".
  */
trait LocalComponent[-Inputs <: Tuple, +Output] extends Component[Inputs, Output]:
  /** Processes the [[Inputs]] and return the [[Output]].
    * @param inputs
    *   the inputs provided to the module.
    * @return
    *   the output of the module.
    */
  def apply[PlacedPeer <: Peer & Constraints](inputs: Inputs): Output on PlacedPeer    

/** Module capable of interacting with the neighbors using an explicit [[Inputs]], and an implicit input coming from the [[Aggregate]] computation.
  *
  * This kind of module requires "neighbor interaction".
  */
trait AggregateComponent[-Inputs <: Tuple, +Output] extends Component[Inputs, Output]:
  /** Processes the [[Inputs]] and return the [[Output]] leveraging an [[Aggregate]] computation.
    * @param inputs
    *   the inputs provided to the module
    * @param aggregate
    *   the aggregate scope for defining aggregate behaviors.
    * @return
    *   the output of the module.
    */
  def apply[PlacedPeer <: Peer & Constraints](inputs: Inputs)(using aggregate: Aggregate): Output on PlacedPeer
