package it.unibo.flexmultimod.core.language

import it.unibo.flexmultimod.core.Peer

/** [[Value]] available on a [[RemotePeer]].
  */
@showAsInfix
infix final case class on[+Value, RemotePeer <: Peer]()

object Language:
  /** Represents the scope of a computation that occurs in the [[PlacedPeer]] context.
    */
  trait Placed[+PlacedPeer <: Peer]

  /** Function used to define the macroprogram that will be executed on the [[PlacedOn]] context. Returns the
    * [[Macroprogram]] application that will be used.
    */
  def program[PlacedOn <: Peer, Result](scope: Placed[PlacedOn] ?=> Result): Macroprogram = ???

  extension [Value, RemotePeer <: Peer, LocalPeer <: Peer](value: Value on RemotePeer)
    /** Given a [[value]] on a [[RemotePeer]], returns the [[value]] on the [[LocalPeer]] context.
      */
    def placed: Placed[LocalPeer] ?=> Value = ???
