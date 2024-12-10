package it.unibo.flexmultimod.language

import it.unibo.flexmultimod.platform.{Macroprogram, Platform}

import scala.annotation.showAsInfix
import it.unibo.flexmultimod.tier.Peer

object FlexMultiModLanguage:
  trait Placed[+PlacedPeer <: Peer]

  @showAsInfix
  infix final case class on[+Value, RemotePeer <: Peer]()

  trait Language:
    def program[PlacedOn <: Peer, Result](scope: Placed[PlacedOn] ?=> Result)(using Platform[PlacedOn]): Macroprogram =
        ???

    def placed[Value, PlacedScope <: Peer, PlacedOn <: Peer](
        value: Value on PlacedOn
    )(using Platform[PlacedScope]): Placed[PlacedScope] ?=> Value = ???

//    inline def programSpec[PlacedOn <: Peer](programScope: (Aggregate, Placed[PlacedOn]) ?=> Unit): Unit
//    def program[PlacedOn <: Peer](programScope: Placed[PlacedOn] ?=> Unit): Unit
//
//    def remoteRef[Value, LocalPeer <: Peer, RemotePeer <: Peer](remoteValue: Value on RemotePeer): Placed[LocalPeer] ?=> Value
//
//    def bind[Value, LocalPeer <: Peer](localValue: Value on LocalPeer): Placed[LocalPeer] ?=> Value
//
//  object Language:
//    import it.unibo.flexmultimod.language.FlexMultiModLanguage.Language

//
//    extension [Value, LocalPeer <: Peer](value: Value on LocalPeer) def bind: Placed[LocalPeer] ?=> Value = ???
//
//    extension [Value, LocalNode <: Peer, RemoteNode <: Peer](remoteValue: Value on RemoteNode)(using placed: Placed[LocalNode])
//      inline def remoteRef: Value = remoteRefMacro(remoteValue)
