# While Language Interpreter

[View Core Components](core-components.md)

This component directly executes Whilelang programs through tree walking interpretation. The interpreter implements operational semantics using Scala's evaluation capabilities while maintaining program state in a mutable environment.

---

## Running the Interpreter

Execute programs using sbt with:

````shell
$ sbt

# Run on a Whilelang source file
sbt> runMain whilelang.interpreter.main sum.while
````

---

## Execution Semantics

The interpreter implements operational semantics through extension methods:

````scala
package whilelang.interpreter

import scala.collection.mutable.Map

// Execution environment (mutable state)
type Environment = Map[String, Int]
given Environment = Map[String, Int]()

// Statement execution logic
extension (stmt: Statement)(using env: Environment)
  def execute: Unit = stmt match
    case If(cond, tSmt, eSmt) => (if cond.value then tSmt else eSmt).execute
    case Write(exp)           => println(exp.value)      // Output expression
    case While(cond, doSmt)   => while cond.value do doSmt.execute  // Loop
    case Print(text)          => println(text)           // Output text
    case SeqStatement(stmts)  => stmts.foreach(_.execute)// Sequence
    case Attrib(id, exp)      => env += id -> exp.value  // Assignment
    case Program(seq)         => seq.execute             // Program entry
    case Skip | _             =>                        // No-op

// Expression evaluation
extension(exp: Expression)(using env: Environment)
  def value: Int = exp match
    case Read                 => io.StdIn.readInt()     // Input
    case Id(id)               => env.getOrElseUpdate(id, 0)  // Variable lookup
    case Integer(value)       => value                  // Literal
    case ExpSum(lhs, rhs)     => lhs.value + rhs.value  // Addition
    case ExpSub(lhs, rhs)     => lhs.value - rhs.value  // Subtraction
    case ExpMult(lhs, rhs)    => lhs.value * rhs.value  // Multiplication

// Boolean evaluation
extension(exp: Bool)(using env: Environment)
  def value: Boolean = exp match
    case Boole(b)             => b                      // Literal
    case ExpEq(lhs, rhs)      => lhs.value == rhs.value // Equality
    case ExpLe(lhs, rhs)      => lhs.value <= rhs.value // Comparison
    case Not(b)               => !b.value               // Negation
    case And(lhs, rhs)        => lhs.value && rhs.value // Conjunction
````

Key characteristics:
- Mutable environment for variable storage
- Strict evaluation semantics
- Direct mapping from AST nodes to execution logic
- Recursive tree walking through pattern matching

---

## Main Entry Point

Coordinates the interpretation pipeline:

````scala
package whilelang.interpreter

import whilelang.util.Runner

// Execution entry point
@main def main(file: String) =
  Runner(program => program.execute)(file)
````

Execution flow:
1. Parses input file using shared [Walker component](core-components.md#walker)
2. Builds AST via shared [parser implementation](core-components.md#parser-implementation)
3. Executes program using interpreter semantics
4. Maintains runtime environment during execution

---

## Differences from Transpiler

While sharing the [core components](core-components.md), the interpreter:
1. Directly evaluates AST nodes instead of generating code
2. Maintains runtime state rather than tracking variables for declaration
3. Uses native Scala I/O operations instead of generating I/O code
4. Implements control flow through Scala's native constructs
````
