package it.unibo.flexmultimod.platform

import it.unibo.flexmultimod.language.Component
import it.unibo.flexmultimod.platform.BoundaryMessages.{InboundMessages, OutboundMessages}
import it.unibo.flexmultimod.tier.Peer

trait Macroprogram

/** Type class abstracting over the specific execution semantics of the [[Platform]].
  *
  * An execution semantic can be round-based of reactive, for example.
  */
trait ExecutionSemantic[F[_]]:
  def execute[Input <: Tuple, Output, A, B](program: Component[Input, Output], context: F[A]): F[B]

enum BoundaryMessages:
  case InboundMessages() extends BoundaryMessages
  case OutboundMessages() extends BoundaryMessages

trait Network[F[_]]:
  def send(message: OutboundMessages): F[Unit]
  def receive(): F[InboundMessages]

trait Platform[F[_], PlacedPeer <: Peer]:
  network: Network =>
  def handleComputation(inputMessages: F[InboundMessages]): F[OutboundMessages]

def run[F[_], PlacedPeer <: Peer](platform: Platform[F, PlacedPeer]): F[Unit] = ???