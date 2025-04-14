# Capability-based refinement placed types

## Motivation

Facing heterogeneous systems, such as CAS (Collective Adaptive Systems) and IoT (Internet of Things) in the Cloud-Edge continuum era,
is a challenging task: the system should account different hosts/devices with different capabilities and resources,
and the different tiers of the system should be able to _properly_ communicate and _interact_ with each other.

For example, in a system where smartphones, wearables, and other devices are involved,
not all the devices can execute the whole (macro)program as supposed in homogeneous systems.
Wearables can provide certain *capabilities* (e.g., GPS, heart rate monitor, etc.),
while smartphones can provide others (e.g., UI, notifications, connectivity etc.).
Moreover, there may be some dependencies between the functionalities provided by the different devices:
- A smartphone in order to provide a notification to the user, it may need to know the heart rate of the user,
- A heavy computation may be executed on a powerful device (e.g., a server) and the result sent to the smartphone to preserve battery life.

It is clear that not all the functionalities required by the program can be executed on the same device,
and for this reason, the system should be partitioned to be able to execute the program.

In this context, pulverization models[^1] help to *partition* the system into different components wired together,
determining the overall (collective) system specification.
However, these models do not capture as a first-class citizen the *placement* of the components,
neither the *capabilities* of the hosts where the components are executed.
They provide a highly flexible way for partitioning the system, but they do not provide a way to specify 
*where* the component can be executed and *what* are the requirements of it in terms of capabilities.

Related approaches, as in the context of multitier programming[^2],
provide a way to specify directly into the function's type the *placement* of it,
letting the compiler checking the validity of the placement.
Even though this approach solves the problem of placement, it does not provide a way to specify the *capabilities* required by the function,
neither cover collective aspects of the system.

With this work, we provide an extension of the concept of *placement types*
with a capability-based refinement type system,
where the type signature of a function includes both the placement and the capabilities required by the function.
This allows to "constrain" the placement of a function to specific classes of hosts implementing the required capabilities,
and to check at compile time the validity of the placement and the availability of the required capabilities.
Finally, collectivity is also supported through a capability,
which allows to specify that a function requires the interaction with the same function instance executed on other (neighboring) hosts,
according to the pulverization model.
In this way, tiers and not only used as a "placeholder" (or phantom type) to validate the placement,
but they contribute to provide a set of capabilities that the function can use to execute.
In other words, the function is *polymorphic over the capabilities* of the host where it is executed,
everything type-checked at compile time.

## Design

We present the design of a capability-based refinement type system for placed types.
The goal is to provide a flexible framework for specifying partitioned (or collective) multitier systems,
where each function (or component) can require certain capabilities to be executed.

Building on the concept of placement types (as introduced in ScalaLoci),
we enrich the type signature of a function with both:
- the host(s) on which it is to be executed, and
- the capabilities required by the function.

This ensures that the host where the function is executed is compatible with the function’s capability requirements.
In this context, a capability is an accessible resource (e.g., a GPS sensor, a router, etc.) that the function depends on.

In addition to verifying valid placements, the type system can also check whether the host provides the required capabilities.
This allows the compiler to catch invalid placements—both in terms of communication between unrelated hosts and missing capabilities—at compile time.

This design neatly separates *where* a function is executed from *what* it needs to execute.
Thanks to this separation, a host is no longer just a phantom type used to validate placement—it becomes an active part of the system, offering functionalities expressed in terms of capabilities.

Capabilities are optional in a function’s type signature.
If omitted, the function can be executed on any compatible host regardless of available capabilities.
This leads to a natural distinction between:
- **Plain functions**, which require no capabilities, and
- **Constrained functions**, which require specific capabilities.

Using this model, we can also trivially encode collective functions through a `Collective` capability.
This enables neighbor-based interactions between hosts.

### Plain functions

A *plain function* does not require any capabilities.
However, it still declares where it can be executed through placement types.

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

### Constrained functions

A *constrained function* requires certain capabilities to be executed.
In addition to placement, its type signature includes the required capabilities.

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

### Collective functions

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

Here, collectiveFunction is a function that:
- requires both `Collective` and `WithGps` capabilities
- can run on either `Smartphone` or `Wearable`
- may interact with neighboring devices during execution

### Macroprogram definition

We refer to *macroprogram* as the system specification where in a single program all the components and their interactions are defined.
To define the interactions between components in a single program, we adopt the concept of "scoping" to determine where a data is produced,
where it will be transferred to other tiers, and where it will be consumed.

Two primitives are used to define the scope of a data:
- `on[Tier]` specifies that the block of code is executed on the specified tier
- `asLocal` specifies that the result of the block of code is "transferred" to the local tier (if the two tiers are connected)

```scala 3
import it.unibo.flexmultimod.demo.MacroApp.{Smartphone, Wearable}

type Smartphone <: { type Tie <: Single[Wearable] }
type Wearable <: { type Tie <: Single[Smartphone] }

def getPosition: (Double, Double) on Wearable :| WithGps = constrained:
  // get the position of the smartphone
  (0.0, 0.0)
  
def distanceFromSource(
    source: Boolean,
    position: (Double, Double)
): Double on (Wearable | Smartphone) :| WithCollective = collective:
  ???
  
def showDistanceOnUi(distance: Double): Unit on Smartphone = plain:
  // show the distance on the smartphone UI
  println(s"Distance: $distance")

def macroProgram = macroprogram[Smartphone]:
    val myPosition = on[Wearable] { getPosition }.asLocal
    val isSource = env[Boolean]("isSource") // access the smartphone environment
    val distance = distanceFromSource(isSource, myPosition).asLocal
    showDistanceOnUi(distance)
```

In this example, the macroprogram specifies that the `Smartphone` is the application device (the tier we want to control),
and the `Wearable` is the infrastructural device.

The `getPosition` function is executed on the `Wearable` tier and returns the position of the smartphone.
The `distanceFromSource` function is executed on either the `Wearable` or `Smartphone` tier and calculates the distance from the source;
in the example, it is executed on the `Smartphone` tier.
The `showDistanceOnUi` function is executed on the `Smartphone` tier and shows the distance on the smartphone UI.

[^1]: Nicolas Farabegoli, Mirko Viroli, & Roberto Casadei (2024). Flexible Self-organisation for the Cloud-Edge Continuum: a Macro-programming Approach. In IEEE International Conference on Autonomic Computing and Self-Organizing Systems, ACSOS 2024, Aarhus, Denmark, September 16-20, 2024 (pp. 21–30). IEEE.
[^2]: Pascal Weisenburger, Johannes Wirth, & Guido Salvaneschi (2021). A Survey of Multitier Programming. ACM Comput. Surv., 53(4), 81:1–81:35.
