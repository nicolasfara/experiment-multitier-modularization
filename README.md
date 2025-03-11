# Multitier and flexible collective programming

Preliminar experiments about mutlitier and flexible collective programming with AC.

## References

- Scalaloci: https://scala-loci.github.io/ -- Multi-tier programming with Scala Loci
- Kyo: https://getkyo.io/ -- Programming with Algebraic Effects

## Modularized Aggregate Computing

```scala 3
trait DistanceTo extends Aggregate:
  def distanceTo[Node <: Peer & WithGps](isSource: Boolean, isTarget: Boolean): Double on Node = ???
  
trait Routing:
  def route[Node <: Peer & WithRouter](target: Node, distance: Double): List[Node] on Node = ???
  
object MyProgram extends Aggregate, DistanceTo, Routing:
  type Application <: Peer { type Tie <: Multiple[Infrastructural] }
  type Infrastructural <: Peer { type Tie <: Multiple[Application] }

  @modularized def programDefinition[Placed <: Peer]() = program[Placed]:
    val distance: Double = distanceTo[Application](env("source") == env.id, env("target") == env.id).bind
    val route: List[Node] = route[Infrastructural](env("target"), distance).bind

@main def run(): Unit =
  Modularized.runOn[MyProgram.Application](MyProgram.programDefinition)
```

https://sebokwiki.org/wiki/Functional_Architecture


- Data specifica dichairativa "compilarla" e verificarla contro un deployment (REPL)
  - Prevedere architettura per questo sistema
- Language per i constraints [~] (behavioral capabilities)
- Typing Aggregate e Scafi (tipo component collettivo) [X]
- Implementazione di un programma collettivo semplice senza ScaFi [~]
