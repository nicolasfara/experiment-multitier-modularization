package it.unibo.flexmultimod.core

/** Generic movable computation.
  */
trait Component[-Inputs <: Product, +Output]:
  /** The constraints required by the [[Component]] to be executed.
    */
  type RequiredCapabilities

  /** Processes the [[Inputs]] and return the [[Output]].
    * @param inputs
    *   the inputs provided to the module.
    * @return
    *   the output of the module.
    */
  def apply[PlacedPeer <: Peer & RequiredCapabilities](inputs: Inputs): Output
