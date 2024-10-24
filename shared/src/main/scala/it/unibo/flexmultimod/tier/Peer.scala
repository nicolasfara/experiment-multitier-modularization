package it.unibo.flexmultimod.tier

trait Peer

/** Expresses the cardinality of a relationship between two peers.
  */
enum Cardinality[PeerType <: Peer]:
  case Single[PType <: Peer]() extends Cardinality[PType]
  case Multiple[PType <: Peer]() extends Cardinality[PType]
  case Optional[PType <: Peer]() extends Cardinality[PType]
