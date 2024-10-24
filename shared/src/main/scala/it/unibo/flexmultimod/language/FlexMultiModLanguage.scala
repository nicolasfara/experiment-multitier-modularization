package it.unibo.flexmultimod.language

import it.unibo.flexmultimod.tier.Peer

import scala.annotation.showAsInfix

object FlexMultiModLanguage:
  trait Placed[+PlacedPeer <: Peer]

  @showAsInfix
  infix final case class on[Value, RemotePeer <: Peer]()

  trait Language:
    def program[PlacedOn <: Peer](programScope: Placed[PlacedOn] ?=> Unit): Unit

    def remoteRef[Value, LocalPeer <: Peer, RemotePeer <: Peer](remoteValue: Value on RemotePeer): Placed[LocalPeer] ?=> Value

    def bind[Value, LocalPeer <: Peer](localValue: Value on LocalPeer): Placed[LocalPeer] ?=> Value

  object Language:
    import it.unibo.flexmultimod.language.meta.LanguageMacro.*

    extension [Value, LocalPeer <: Peer](value: Value on LocalPeer) def bind: Placed[LocalPeer] ?=> Value = ???

    extension [Value, LocalNode <: Peer, RemoteNode <: Peer](remoteValue: Value on RemoteNode)(using placed: Placed[LocalNode])
      inline def remoteRef: Value = remoteRefMacro(remoteValue)
