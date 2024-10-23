package it.unibo

import it.unibo.SharedCode
import org.scalajs.dom

@main
def run(): Unit =
  dom.console.log("Hello from Scala 3!")
  dom.console.log(SharedCode.sharedMessage)
