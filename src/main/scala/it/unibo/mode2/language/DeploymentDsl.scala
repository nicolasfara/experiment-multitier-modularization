package it.unibo.mode2.language

import it.unibo.mode2.MovementDetection
import it.unibo.mode2.language.DeploymentDsl.on

import scala.annotation.showAsInfix

/** Tentative DSL for binding components:
  * {{{
  *   deployment:
  *     forDevice(D0) deploy (C1 on D0 :: C2 on D0 :: C3 on E0 :: Nil)
  *     forDevice(D1) deploy (C1 on D1 :: C2 on D1 :: C3 on E0 :: Nil)
  *     forDevice(D2) deploy (C1 on D2 :: C2 on D2 :: C3 on E0 :: Nil)
  *
  *   // Deployment(
  *   //   Deploy(D0, on(C1, D0) :: on(C2, D0) :: on(C3, E0) :: Nil),
  *   //   Deploy(D1, on(C1, D1) :: on(C2, D1) :: on(C3, E0) :: Nil),
  *   //   Deploy(D2, on(C1, D2) :: on(C2, D2) :: on(C3, E0) :: Nil),
  *   // )
  * }}}
  */
object DeploymentDsl:
  @showAsInfix
  infix final case class On[Capabilities, C <: Component[?, ?, Capabilities], D <: Device[?, Capabilities]](
      component: C,
      device: D,
  )
  extension [Capabilities, C <: Component[?, ?, Capabilities], D <: Device[?, Capabilities]](c: C)
    infix def on(d: D): On[Capabilities, C, D] = On(c, d)

  final case class DeploymentSpec()
  final class Deployment

  def deployment(init: Deployment ?=> Unit): DeploymentSpec = ???

  infix def deploy[ID: Ordering](
      device: Application[ID, ?],
      allocations: List[On[?, ?, ?]]
  )(using d: Deployment) = ???

  def foo = deployment:
    val a = MovementDetection on Smartphone1()
