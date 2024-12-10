package it.unibo.flexmultimod.language

import it.unibo.flexmultimod.platform.Platform
import it.unibo.flexmultimod.tier.Peer

class ModularizedApp[PlacedPeer <: Peer]:
  given Platform[PlacedPeer] = ???

  final def run(args: List[String]): Unit = ???
