package it.unibo.flexmultimod.core.language

import it.unibo.flexmultimod.core.Peer
import it.unibo.flexmultimod.core.language.meta.LanguageMacro.programMacroImpl

import scala.annotation.showAsInfix

object Language:
  /** Represents the scope of a computation that occurs in the [[PlacedPeer]] context.
    */
  trait Placed[+PlacedPeer <: Peer]

//  private final case class onValue[+Value](value: Value)
//
//  @showAsInfix
//  infix opaque type on[+Value, RemotePeer <: Peer] = onValue[Value]

  /** Function used to define the macroprogram that will be executed on the [[PlacedOn]] context. Returns the
    * [[Macroprogram]] application that will be used.
    */
  inline def program[PlacedOn <: Peer, Result](inline scope: Placed[PlacedOn] ?=> Result): Macroprogram =
    // given p: Placed[PlacedOn] = new Placed[PlacedOn] {}
    ${ programMacroImpl('scope) }

//  extension [Value, RemotePeer <: Peer, LocalPeer <: Peer](value: Value)
//    /** Given a [[value]] on a [[RemotePeer]], returns the [[value]] on the [[LocalPeer]] context.
//      */
//    def placed: Placed[LocalPeer] ?=> Value = ???

  extension [Value, LocalPeer <: Peer](value: Aggregate[Value])
    /** Given a [[value]] on a [[RemotePeer]], returns the [[value]] on the [[LocalPeer]] context coming from an
      * aggregation operation.
      */
    def localValue: Placed[LocalPeer] ?=> Value = ???
