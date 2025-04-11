# Capability-based refinement placed types

We present the design of a capability-based refinement type for placed types.
The goal is to provide a flexible framework for specifying partitioned (also collective) multitier systems
where each function (or component) can require certain capabilities to be executed.
Similar to the concept of "placement types" (as presented in ScalaLoci),
we enrich the type signature of a function with the host(s) on which it will be executed,
plus the capabilities required by the function.
In this way, the "place" on which the function will be executed must be compatible with the capabilities required by the function.
In this domain, a capability is an accessible resource (e.g., a GPS sensor, a router, etc.) that can be used by the function.

Other than checking valid placements, the type system can also check if the host on which the function is executed
can provide the required capabilities,
intercepting at compile time wrong placements both in terms of communication between two non-tied hosts,
and in terms of missing capabilities.
With this design, we neatly separate "where" the function is executed from "what" is required to execute it.
Thanks to this separation, the hosts is not only a phantom type used to validate the placement,
but it is an active part of the system providing functionalities expressed in terms of capabilities.

Capabilities are not mandatory in the type signature of a function,
meaning that a function has no restrictions on which host it can be executed.
For this reason, we can distinguish between "plain" functions (requiring no capabilities) and "constrained" functions
(requiring capabilities).

With this model, it is possible to trivially encode "collective" functions via the use of a "collective" capability.
This capability can be employed to enable collective behavior, namely enable a neighbor-based interaction.

## Plain functions

As introduced in the previous section, a plain function is a function that does not require any capability to be executed.
Nevertheless, it must define on which possible host it can be executed via placement types.

```scala 3
import it.unibo.flexmultimod.demo.MacroApp.{Smartphone, Wearable}

// Architecture definition (a la ScalaLoci)
type Smartphone <: { type Tie <: Single[Wearable] }
type Wearable <: { type Tie <: Single[Smartphone] }

def plainFunction(input: Int): Int on Smartphone | Wearable = plain:
  input * 2
```

In this example, the function `plainFunction` is a plain function that takes an `Int` as input and returns an `Int`.
No capabilities are required to execute this function.

The function can be executed on both `Smartphone` and `Wearable` hosts.
To express this placement constraint, we use the `|` operator expressing the fact that the function can be either placed on `Smartphone` or `Wearable`.

## Constrained functions

A constrained function is a function that requires certain capabilities to be executed.
In its type signature, other than the placement type, it also specifies the capabilities required by the function.

```scala 3
import it.unibo.flexmultimod.demo.WithGps
type Smartphone <: WithGps & { type Tie <: Single[Wearable] }
type Wearable <: WithGps & { type Tie <: Single[Smartphone] }

trait WithGps:
  def getCoordinates: (Double, Double)

def constrainedFunction(pos: (Double, Double)): Double on (Smartphone | Wearable) :| WithGps = constrained:
  val currentPosition = capability[WithGps].getCoordinates
  val distance = math.sqrt(math.pow(currentPosition._1 - pos._1, 2) + math.pow(currentPosition._2 - pos._2, 2))
  distance
```

The `constrainedFunction` produces a `Double` value, it can be placed either on `Smartphone` or `Wearable`,
and it requires the `WithGps` capability to be executed, meaning that `Smartphone` and `Wearable`
must implement the `WithGps` trait.

If the function is placed on a host that does not provide the required capability,
the type checker will raise an error.

```scala 3
import it.unibo.flexmultimod.demo.WithRouter
type Smartphone <: WithRouter & { type Tie <: Single[Wearable] }
type Wearable <: WithRouter & { type Tie <: Single[Smartphone] }

trait WithRouter:
  def getRoute: List[String]
  
trait WithGps:
  def getCoordinates: (Double, Double)

def constrainedFunction(pos: (Double, Double)): Double on Smartphone :| WithGps = constrained:
  val currentPosition = capability[WithGps].getCoordinates
  val distance = math.sqrt(math.pow(currentPosition._1 - pos._1, 2) + math.pow(currentPosition._2 - pos._2, 2))
  distance
```

Here, the `constrainedFunction` is placed on `Smartphone`, which provides the `WithRouter` capability,
but not the `WithGps` capability.

## Collective functions

A collective function is a function that requires the `Collective` capability to be executed.
This capability is used to enable collective behavior, meaning that the function can be executed on multiple hosts
and can interact with other hosts in the system.

```scala 3
import it.unibo.flexmultimod.demo.{WithCollective, WithGps}

type Smartphone <: WithCollective & { type Tie <: Single[Wearable] }
type Wearable <: WithCollective & { type Tie <: Single[Smartphone] }

trait WithCollective
trait WithGps:
  def getCoordinates: (Double, Double)

def collectiveFunction(input: Int): Int on (Smartphone | Wearable) :| WithCollective & WithGps = collective:
  // do something with capability[WithCollective] and capability[WithGps]
  ???
```

In this example, the `collectiveFunction` is a collective function that takes an `Int` as input and returns an `Int`,
but the final result will be computed in a collective way,
namely requiring the interaction with neighbor devices on which the function is executed.