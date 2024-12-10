package it.unibo.flexmultimod.tier

trait Peer

/** Expresses the cardinality of a relationship between two peers.
  */
enum Cardinality[P <: Peer]:
  case Single[PlacedPeer <: Peer]() extends Cardinality[PlacedPeer]
  case Multiple[PlacedPeer <: Peer]() extends Cardinality[PlacedPeer]
  case Optional[PlacedPeer <: Peer]() extends Cardinality[PlacedPeer]
