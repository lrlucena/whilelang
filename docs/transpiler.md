# While Language Transpiler

This component translates Whilelang programs into equivalent Scala code, enabling execution on the JVM through source-to-source compilation. The transpiler maintains the original program semantics while leveraging Scala's syntax and runtime environment.

---

## Transpilation Process

Execute the transpiler using sbt with:

````shell
$ sbt

# Convert Whilelang program to Scala
sbt> runMain whilelang.compiler.Main sum.while
````

This generates Scala code that can be compiled and executed using standard Scala tools.

---

## Code Comparison Examples

<table>
  <thead>
    <tr>
      <th>Program</th>
      <th>Whilelang</th>
      <th>Generated Scala</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Hello World</td>
      <td>
```ruby
print "Hello World"
```
      </td>
      <td>
```scala
@main def main() = 
  println("Hello World")
```
      </td>
    </tr>

    <tr>
      <td>Sum of Numbers</td>
      <td>
```ruby
print "Enter the first number:";
a := read;
print "Enter the second number:";
b := read;
sum := a + b;
print "The sum is:";
write sum
```
      </td>
      <td>
```scala
@main def main() = 
  var a, b, sum = 0
  println("Enter the first number:")
  a = readInt()
  println("Enter the second number:")
  b = readInt()
  sum = a + b
  println("The sum is:")
  println(sum)
```
      </td>
    </tr>

    <tr>
      <td>Fibonacci Sequence</td>
      <td>
```ruby
print "Fibonacci Sequence";
a := 0;
b := 1;
while b <= 1000000 do {
  write b;
  b := a + b;
  a := b - a
}
```
      </td>
      <td>
```scala
@main def main() = 
  var a, b = 0
  println("Fibonacci Sequence")
  a = 0
  b = 1
  while b <= 1000000 do
    println(b)
    b = a + b
    a = b - a
```
      </td>
    </tr>
  </tbody>
</table>

Key translation patterns:

- `print` statements become `println` with string literals
- `read` operations map to `readInt()` calls
- While loops preserve their structure
- Variable declarations are hoisted with default zero values

---

## Translation Semantics

The core translation logic uses extension methods to convert AST nodes to Scala code:

```scala linenums="1"
--8<--
src/main/scala/whilelang/compiler/Semantics.scala
--8<--
```

````scala
package whilelang.compiler

type Ids = Set[String]  // Track declared variables

extension (src: Any)(using ids: Ids)
  def meaning: String = src match
    case If(cond, tSmt, eSmt) => 
      s"if ${cond.m} then\n  ${tSmt.m}\nelse\n  ${eSmt.m}"
    case Attrib(id, exp) => 
      ids.add(id); s"$id = ${exp.m}"  // Track variable declarations
    case Program(seq) => 
      s"@main def main() =\n  ${vars(ids)}\n  ${seq.m}"
    // Other cases handle remaining node types...
````

Notable features:

- **Variable Tracking**: Automatically detects variable declarations
- **String Building**: Recursively constructs Scala source strings
- **Syntax Preservation**: Maintains original program structure where possible
- **Type Safety**: Uses pattern matching on sealed AST types

---

## Parser Implementation

Shared with the interpreter, the ANTLR listener constructs the AST:

````scala
class MyListener extends WhilelangBaseListener with ContextValue:
  override def exitAttrib(ctx: AttribContext) =
    ctx.value = Attrib(ctx.ID.text, ctx.expression.value)

  override def exitWhile(ctx: WhileContext) =
    ctx.value = While(ctx.bool.value, ctx.statement.value)
  
  // Other rules handle remaining language constructs
````

The parser:

- Uses the same grammar as the interpreter
- Constructs identical AST structure
- Enables code reuse between interpreter and transpiler

---

## Main Entry Point

The transpiler entry point generates and prints Scala code:

```scala linenums="1"
--8<--
src/main/scala/whilelang/compiler/Main.scala
--8<--
```

````scala
package whilelang.compiler

def action = Runner(program => println(program.meaning))

@main def main(file: String) = action(file)
````

Execution flow:

1. Parses Whilelang source file
2. Builds AST using shared parser rules
3. Converts AST to Scala code via `meaning` extension
4. Outputs generated Scala to console

---

## Shared Utilities

### Runner Class

Handles file processing and error management:

````scala
object Runner:
  def apply(action: Program => Unit)(file: String) = 
    sourceCode(file).flatMap(walk) match
      case Success(program) => action(program)
      case Failure(e) => handleError(e)
````

### Walker Component

Reused from interpreter implementation:

````scala
object Walker:
  def walk(source: String)(using listener: MyListener) = Try:
    val lexer = WhilelangLexer(CharStreams.fromString(source))
    val parser = WhilelangParser(CommonTokenStream(lexer))
    ParseTreeWalker().walk(listener, parser.program)
    listener.program
````

### ContextValue Trait

Provides tree property management for AST construction:

````scala
trait ContextValue:
  given ParseTreeProperty[Any] = ParseTreeProperty[Any]()
  
  extension (tree: ParseTree)
    def value[E]: E = // Retrieves typed AST node
    def value_=(v: Any) = // Stores parsed value
````

---

This transpiler demonstrates:

- Source-to-source compilation techniques
- AST-based code generation
- Shared infrastructure with interpreter
- Idiomatic Scala code generation
- Automatic variable management
- Structural preservation of control flow

The generated Scala code maintains the original program's behavior while benefiting from Scala's type system and runtime environment.
