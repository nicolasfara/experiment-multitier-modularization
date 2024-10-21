package it.nicolasfarabegoli.nicolasfarabegoli

import it.nicolasfarabegoli.SharedCode
import org.scalajs.dom

@main
def run(): Unit =
  dom.console.log("Hello from Scala 3!")
  dom.console.log(SharedCode.sharedMessage)
