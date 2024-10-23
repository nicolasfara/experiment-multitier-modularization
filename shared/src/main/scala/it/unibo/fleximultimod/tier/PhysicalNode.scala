package it.unibo.fleximultimod.tier

trait PhysicalNode

/** Expresses the cardinality of a relationship between two peers.
  */
enum Cardinality[Node <: PhysicalNode]:
  case Single[N <: PhysicalNode]() extends Cardinality[N]
  case Multiple[N <: PhysicalNode]() extends Cardinality[N]
  case Optional[N <: PhysicalNode]() extends Cardinality[N]

infix trait TiedWith[Peer <: PhysicalNode, Tie <: Cardinality[?]]