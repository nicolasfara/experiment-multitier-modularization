# Capability-based refinement placed types

## Motivation

Designing systems in the Cloud-Edge continuum—such as Collective Adaptive Systems (CAS) and the Internet of Things (IoT)—presents significant challenges.
These systems consist of heterogeneous hosts and devices with diverse capabilities and resources, and must support proper communication and interaction across different tiers.

Traditional CASs suppose to be employed in homogeneous networks,
where the same characteristics in terms of communication, sensing/actuation, and computation are shared by all the devices,
supposing that all the devices can execute entirely the same program specification.
On these assumptions, relevant frameworks such as Aggregate Computing[^3] has been proposed to program collective, large-scale, scalable systems.
This paradigm allows to define the large-scale collective behavior of the system in a single (macro)program,
leveraging spatio-temporal abstractions for encoding the system behavior.
These collective abstractions guarantee self-* properties, such as self-organization, self-healing, and self-adaptation,
making the system highly reliable and scalable even in the presence of device failures, network disconnections, and other unexpected disruptive events.

Homogeneous assumptions do not hold in non-homogeneous systems, such as the Cloud-Edge continuum.
In these infrastructures, the device heterogeneity is the norm rather than the exception;
even if from one side this heterogeneity can be opportunistically exploited to improve the system performance,
from the other side it requires a shifting of the programming paradigm to a more flexible and modular one.

Engineering such "partitioned" systems requires extra care in the design phase, preserving these properties:
- **Functional equivalence**: the partitioned (macro)program should eventually behave like an equivalent monolithic version,
- **Placement coherence**: the placement of the components should be coherent with the system architecture specification,
- **Capability coherence**: the placement of the components should be coherent with the capabilities offered by the hosts.

When engineering a collective system in the ECC, the aforementioned properties must be enforced at the design level to guarantee the system's reliability and performance,
other than preserving the resilience and self-* properties of the system.

[//]: # ()
[//]: # (In contrast to homogeneous systems, where each device can execute the entire macroprogram, heterogeneous systems require a different approach.)

[//]: # (For instance, wearables may offer capabilities like GPS or heart rate monitoring, while smartphones provide UI, connectivity, or notification services.)

[//]: # (Furthermore, some functionalities may depend on each other:)

[//]: # (- A smartphone might need heart rate data from a wearable to trigger a notification,)

[//]: # (- Intensive computation might be offloaded to a powerful server, with results returned to a smartphone to preserve battery life.)

[//]: # ()
[//]: # (Clearly, not all functionalities can be executed on a single device.)

[//]: # (Consequently, the system must be partitioned to enable distributed execution of the macroprogram.)

[//]: # ()
[//]: # (Pulverization models[^1] help to *partition* the system into interconnected components,)

[//]: # (determining the overall &#40;collective&#41; system specification.)

[//]: # (However, these models do not capture as a first-class citizen the *placement* of the components,)

[//]: # (neither the *capabilities* of the hosts where the components are executed.)

[//]: # (They provide a highly flexible way for partitioning the system, but they do not provide a way to specify )

[//]: # (*where* the components can be executed and *what* are the capabilities each component requires.)

[//]: # ()
[//]: # (Similarly, multitier programming approaches[^2] embed placement directly in a function's type,)

[//]: # (enabling compile-time validation of placements.)

[//]: # (While effective in addressing where a function can run, these models still do not specify which capabilities are required,)

[//]: # (nor do they account for collective behavior among components.)

### Our contribution

In this work, we propose a **capability-based refinement type system** for placed types,
for programming collective systems where once deployed in the continuum,
the execution of the program is scattered across multiple heterogeneous devices,
while preserving the same functional behavior of the system.

The main goal is to preserve safety at the design level,
providing the programmer a way to explicitly specify **where** a function can be placed,
while constraining such "placement" with the **capabilities** required by the function to be executed,
specifying **what** the function requires to be executed.

Encoding such properties in the type system allows to catch errors at compile time,
guaranteeing that the system is coherent with the architecture specification,
and that the components are placed on hosts that provide the required capabilities,
guaranteeing the last two properties mentioned above.
The first property is partially covered by functions composition,
where the type system checks that the output of a function is compatible with the input of another function.
The remainder of the property will be covered by the underlying platform (or middleware),
which will take care of resemble the distributed communication between the components.

We extends the concept of _placement types_ by integrating a **capability-based refinement type system**.
In our approach:
1. A function's type signature includes both its _placement_ and the _capabilities_ it requires,
2. this allows the system to constrain function execution to hosts that satisfy both placement and capability requirements,
3. encode collective behavior through _capabilities_.

We consider three types of functions:

- **Plain functions**: These functions do not require any capabilities. They can be executed on any compatible host.
- **Constrained functions**: These functions require specific capabilities to be executed.
  The type signature includes the required capabilities, and the system checks whether the host provides them.
- **Collective functions**: These functions require the _collective_ capability to be executed.
  This enables neighbor-based interactions between hosts.

With this design, tiers are not only used as a "placeholder" (or phantom type) to validate the placement,
but they contribute to provide a set of capabilities that the function can use to execute.

Finally, thanks to the concept of _capabilities_ we can define **polymorphic functions** over the capabilities of the host;
this means that the actual implementation of the function can be different depending on the capabilities of the host where it is executed,
preserving the same type signature.

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

### Capability-controlled spatial interaction

In heterogeneous systems like the ones we are considering,
when specific collective operations are involved, we are interested to interact with neighboring devices providing certain capabilities.
For example, a collective operation involving the share of the temperature sensor may involve only the devices equipped with a temperature sensor, excluding from the interaction the others.

In this case, through the capability mechanism, we can control the interaction with the neighboring devices.
This approach recalls the "branching" operator of Aggregate Computing, where the network is partitioned based on a certain boolean condition.
In our design, we can achieve a similar behavior but encoded at the type level: in the signature, the required capabilities to perform the interaction are specified.
If no capabilities are specified, the function can interact with all the neighbors without any restriction.

```scala 3
trait TempSensor:
  def getTemperature: Double

def capabilityControlledCollectiveFunction(): Int on Smartphone :| WithCollective & TempSensor = collective:
  val capValue: Double :| TempSensor = capability[TempSensor].getTemperature
  val onlyTemperatures = neighbors[Double :| TempSensor](capValue)
  val plainCollective = neighbors[Double](10.0)
  ???
```

Based on the example above, `onlyTemperatures` represents a collective data structure only provided by neighbor devices having the `TempSensor` capability.
The `plainCollective` represents a collective data structure that can be provided by all the neighbors, regardless of their capabilities.
Even if some similarities with the `branch` operator can be found, here we do not want to specify two possible branches of the program (or in other words, partition the network),
we are just specifying that the collective result strictly depends on the specified capability.

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

## Related

- _Effects as capabilities: effect handlers and lightweight effect polymorphism_: https://doi.org/10.1145/3428194
- _Capabilities: Effects for Free_: https://doi.org/10.1007/978-3-030-02450-5_14
- _Effects, Capabilities, and Boxes_: https://doi.org/10.1145/3527320
- _Refinement types for Haskell_:  https://doi.org/10.1145/2692915.2628161

[^1]: Nicolas Farabegoli, Mirko Viroli, & Roberto Casadei (2024). Flexible Self-organisation for the Cloud-Edge Continuum: a Macro-programming Approach. In IEEE International Conference on Autonomic Computing and Self-Organizing Systems, ACSOS 2024, Aarhus, Denmark, September 16-20, 2024 (pp. 21–30). IEEE.
[^2]: Pascal Weisenburger, Johannes Wirth, & Guido Salvaneschi (2021). A Survey of Multitier Programming. ACM Comput. Surv., 53(4), 81:1–81:35.
[^3]: Beal, J., Pianini, D., & Viroli, M. (2015). Aggregate Programming for the Internet of Things. Computer, 48(9), 22-30.
