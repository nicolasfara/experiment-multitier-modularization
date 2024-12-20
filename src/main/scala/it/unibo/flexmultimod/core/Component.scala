package it.unibo.flexmultimod.core

import it.unibo.flexmultimod.core.Peer

/** Generic movable computation.
  */
trait Component[-Inputs <: Tuple, +Output, RequiredCapabilities]:
  /** The constraints required by the [[Component]] to be executed.
    */
  final type WithRequiredCapabilities = Peer & RequiredCapabilities

  /** Processes the [[Inputs]] and return the [[Output]].
    * @param inputs
    *   the inputs provided to the module.
    * @return
    *   the output of the module.
    */
  def apply[PlacedPeer <: WithRequiredCapabilities](inputs: Inputs): Output
