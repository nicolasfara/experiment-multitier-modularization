package it.unibo.flexmultimod.core

import it.unibo.flexmultimod.core.language.Language.on

/** Generic movable computation.
  */
trait Component[-Inputs <: Tuple, +Output]:
  /** The constraints required by the [[Component]] to be executed.
    */
  type RequiredCapabilities

  /** Processes the [[Inputs]] and return the [[Output]].
    * @param inputs
    *   the inputs provided to the module.
    * @return
    *   the output of the module.
    */
  def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: Inputs): Output on PlacedPeer
