package it.unibo.fleximultimod.language

import scala.annotation.showAsInfix

object FlexMultiModLanguage:
  trait Placed[+Peer]

  @showAsInfix
  infix final case class on[Value, RemotePeer]()
  
  trait Language:  
    def program[Peer](programScope: Placed[Peer] ?=> Unit): Unit
  
    def remoteRef[Value, LocalPeer, RemotePeer](remoteValue: Value on RemotePeer): Placed[LocalPeer] ?=> Value
  
    def bind[Value, LocalPeer](localValue: Value on LocalPeer): Placed[LocalPeer] ?=> Value

//object Language:
//
//  given foo[Value, Node: ]: Conversion[on[Value, Node], Value] with
//    def apply(value: on[Value, Node]): Value = ???
//
//  def program[Node](programScope: Placed[Node] ?=> Unit): Unit =
//    ???
//
//  extension [Value, LocalNode](value: Value on LocalNode) def bind: Placed[LocalNode] ?=> Value = ???
//
//  extension [Value, LocalNode, RemoteNode](value: Value on RemoteNode)(using placed: Placed[LocalNode])
//    inline def asLocal: Value =
//      import it.unibo.fleximultimod.language.meta.LanguageMacro._
//      asLocalMacro(value)
