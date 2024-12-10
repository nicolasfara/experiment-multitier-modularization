package it.unibo.flexmultimod.core

/** Represents a peer in the system.
  */
sealed trait Peer
trait ApplicationPeer extends Peer
trait InfrastructuralPeer extends Peer

/** Expresses the cardinality of a relationship between two peers.
  */
enum Cardinality[P <: Peer]:
  case Single[PlacedPeer <: Peer]() extends Cardinality[PlacedPeer]
  case Multiple[PlacedPeer <: Peer]() extends Cardinality[PlacedPeer]
  case Optional[PlacedPeer <: Peer]() extends Cardinality[PlacedPeer]
