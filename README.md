# While Language

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b1705795c5f74b9289b6f4c942dd5911)](https://app.codacy.com/gh/lrlucena/whilelang)
> A minimalistic programming language built using [Scala 3.4](https://scala-lang.org) and [ANTLR 4.13](https://antlr.org).

This simple programming language features a single loop construct (`while`) and supports only integer types. 

The language is implemented in two distinct ways:
- As an [interpreter](interpreter.md)
- As a [transpiler](transpiler.md) (source-to-source compiler) targeting Scala.

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
        <a href="#grammar">Grammar</a> (29 lines)
      </td>
    </tr>
    <tr>
      <th>Abstract Syntax</th>
      <td colspan="2" align="center">
        <a href="interpreter.md#abstract-syntax">Abstract Syntax</a> (23 lines)
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

## Examples
Below are some example programs:

### Hello World
````text
print "Hello World"
````

### Sum of Two Numbers
````text
print "Enter the first number:";
a := read;
print "Enter the second number:";
b := read;
sum := a + b;
print "The sum is: ";
print sum
````

### Fibonacci Sequence
````text
print "Fibonacci Sequence";
a := 0;
b := 1;
while b <= 1000000 do {
  print b;
  b := a + b;
  a := b - a
}
````

## Grammar
The formal syntax, defined in ANTLR, is as follows:

````antlr
grammar Whilelang;

program : seqStatement;

seqStatement: statement (';' statement)* ;

statement: ID ':=' expression                          # attrib
         | 'skip'                                      # skip
         | 'if' bool 'then' statement 'else' statement # if
         | 'while' bool 'do' statement                 # while
         | 'print' Text                                # print
         | 'print' expression                          # write
         | '{' seqStatement '}'                        # block
         ;

expression: INT                                        # int
          | 'read'                                     # read
          | ID                                         # id
          | expression '*' expression                  # binOp
          | expression ('+'|'-') expression            # binOp
          | '(' expression ')'                         # expParen
          ;

bool: ('true'|'false')                                 # boolean
    | expression '=' expression                        # relOp
    | expression '<=' expression                       # relOp
    | 'not' bool                                       # not
    | bool 'and' bool                                  # and
    | '(' bool ')'                                     # boolParen
    ;

INT: ('0'..'9')+ ;
ID: ('a'..'z')+;
Text: '"' .*? '"';
Space: [ \t\n\r] -> skip;
````

---

## Compiling and Running

To compile the project, you'll need to install [sbt](https://www.scala-sbt.org/). The easiest installation method is via [SDKMAN!](https://sdkman.io/install) (Linux) or [Scoop](https://scoop.sh/) (Windows).

````shell
$ sbt
sbt> clean
sbt> compile

# Run the interpreter
sbt> runMain whilelang.interpreter.main sum.while

# Run the transpiler
sbt> runMain whilelang.compiler.main sum.while
````
