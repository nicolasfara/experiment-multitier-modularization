package it.unibo.fleximultimod.tier

trait PhysicalNode

/** Expresses the cardinality of a relationship between two peers.
  */
enum Cardinality[Node]:
  case Single[N]() extends Cardinality[N]
  case Multiple[N]() extends Cardinality[N]
  case Optional[N]() extends Cardinality[N]

infix trait TiedWith[Peer <: PhysicalNode, Tie <: Cardinality[?]]
