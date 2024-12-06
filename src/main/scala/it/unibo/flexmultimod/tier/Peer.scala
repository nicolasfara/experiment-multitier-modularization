package it.unibo.flexmultimod.tier

trait Peer

/** Expresses the cardinality of a relationship between two peers.
  */
trait Single[PType <: Peer]
trait Multiple[PType <: Peer]
trait Optional[PType <: Peer]
