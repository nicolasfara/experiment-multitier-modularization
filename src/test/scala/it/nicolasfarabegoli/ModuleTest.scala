package it.nicolasfarabegoli

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should

class ModuleTest extends AnyFreeSpec, should.Matchers:
  "A module with no capabilities should be able to run on any peer" in:
    """
    trait Cap1
    type MyPeer = Peer & Cap1
    type MyOtherPeer = Peer
    object MyComponent extends LocalComponent[(Int, Int), String]:
      override type Constraints = Any
      override def apply[PlacedPeer <: Peer & Constraints](inputs: (Int, Int)): String on PlacedPeer = ???

    MyComponent[MyPeer]((1, 1))
    MyComponent[MyOtherPeer]((1, 1))
    """ should compile

  "A module with capabilities should be able to run only on peers with those capabilities" in:
    """
    trait Cap1
    trait Cap2
    type MyPeer = Peer & Cap1
    type MyOtherPeer = Peer & Cap2
    object MyComponent extends LocalComponent[(Int, Int), String]:
      override type Constraints = Cap1
      override def apply[PlacedPeer <: Peer & Constraints](inputs: (Int, Int)): String on PlacedPeer = ???

    MyComponent[MyPeer]((1, 1))
    MyComponent[MyOtherPeer]((1, 1))
    """ shouldNot compile
