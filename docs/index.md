# WhileLang

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b1705795c5f74b9289b6f4c942dd5911)](https://app.codacy.com/gh/lrlucena/whilelang)

A minimalistic programming language built using [Scala 3.6](https://scala-lang.org) and [ANTLR 4.13](https://antlr.org).

## Features

- Single loop construct (`while`)
- Integer types only
- Two implementations:
  - [Interpreter](interpreter.md)
  - [Transpiler](transpiler.md) to Scala

## Implementation Comparison

<table>
  <thead>
    <tr>
      <th></th>
      <th align="center">Interpreter</th>
      <th align="center">Transpiler (Compiler)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>Grammar</th>
      <td colspan="2" align="center">
        <a href="#grammar">Grammar</a>
      </td>
    </tr>
    <tr>
      <th>Abstract Syntax</th>
      <td colspan="2" align="center">
        <a href="interpreter.md#abstract-syntax">Abstract Syntax</a>
      </td>
    </tr>
    <tr>
      <th>Semantics</th>
      <td align="center"><a href="interpreter.md#semantics">Semantics</a> (35 lines)</td>
      <td align="center"><a href="transpiler.md#semantics">Semantics</a> (34 lines)</td>
    </tr>
    <tr>
      <th>Parser Rules</th>
      <td colspan="2" align="center">
        <a href="interpreter.md#parser-rules">Listener</a> (56 lines)
      </td>
    </tr>
    <tr>
      <th>Main</th>
      <td align="center"><a href="interpreter.md#main">Main</a> (4 lines)</td>
      <td align="center"><a href="transpiler.md#main">Main</a> (4 lines)</td>
    </tr>
    <tr>
      <th>Utility Classes</th>
      <td colspan="2" align="center">
        <a href="interpreter.md#walker">Walker</a> (20 lines)<br>
        <a href="interpreter.md#runner">Runner</a> (14 lines)<br>
        <a href="interpreter.md#contextvalue">ContextValue</a> (13 lines)
      </td>
    </tr>
    <tr>
      <th>Total</th>
      <td align="center">194 lines</td>
      <td align="center">193 lines</td>
    </tr>
  </tbody>
</table>

## Example Programs

### Hello World

```python linenums="1" title="hello.while"
print "Hello World"
```

### Sum of Two Numbers

```python linenums="1" title="sum.while"
print "Enter the first number:";
a := read;
print "Enter the second number:";
b := read;
sum := a + b;
print "The sum is: ";
print sum
```

### Fibonacci Sequence

```python linenums="1" title="examples/fibonacci.while"
print "Fibonacci Sequence";
a := 0;
b := 1;
while b <= 1000000 do {
  print b;
  b := a + b;
  a := b - a
}
```

## Getting Started

### Prerequisites

- [sbt](https://www.scala-sbt.org/)
- [Java JDK](https://adoptium.net/) (version 11 or higher recommended)

### Installation

=== "Linux/macOS"
    ```sh
    curl -s "https://get.sdkman.io" | bash
    sdk install sbt
    ```

=== "Windows"
    ```powershell
    scoop install sbt
    ```

### Building and Running

1. Clone the repository:
   ```sh
   git clone https://github.com/lrlucena/whilelang.git
   cd whilelang
   ```

2. Run the interpreter:
   ```sh
   sbt "runMain whilelang.interpreter.main examples/sum.while"
   ```

3. Run the transpiler:
   ```sh
   sbt "runMain whilelang.compiler.main examples/fib.while"
   ```

## Project Structure

```tree
whilelang/
├── src/
│   └── main/
│       ├── scala/whilelang/
│       │   ├── compiler/      # Transpiler implementation
│       │   ├── interpreter/   # Interpreter implementation
│       │   └── parser/        # Parser files
|       └── antlr4/whilelang/
|           └── parser/        # ANTLR grammar
├── examples/                  # Sample While programs
└── project/                   # sbt build files
```
