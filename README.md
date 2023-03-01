# While language

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b1705795c5f74b9289b6f4c942dd5911)](https://www.codacy.com/app/leonardo-lucena/whilelang?utm_source=github.com&utm_medium=referral&utm_content=lrlucena/whilelang&utm_campaign=badger)

> A small programming language made with [Scala](https://scala-lang.org) and [ANTLR](https://antlr.org).

This is a simple programming language that has only one loop instruction (while) and a single type (integer).
We want to show that you can make a programming language with a only few lines of code.

The language is implemented in two ways:
 - as an [interpreter](interpreter.md)
 - as a [transpiler](transpiler.md) (compiler) for Scala.

<table>
  <thead>
    <tr>
      <th> </th>
      <th align="center">Interpreter</th>
      <th align="center">Compiler (Transpiler)</th>
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
      <td align="center"><a href="transpiler.md#semantics">Semantics</a> (33 lines)</td>
    </tr>
    <tr>
      <th>Parser Rules</th>
      <td colspan="2" align="center">
        <a href="interpreter.md#parser-rules">Listener</a> (58 lines)
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
      <a href="interpreter.md#walker">Walker</a> (21 lines)<br>
      <a href="interpreter.md#runner">Runner</a> (12 lines)<br>
      <a href="interpreter.md#contextvalue">Runner</a> (9 lines)
      </td>
    </tr>
    <tr>
      <th>Total</th>
      <td align="center">191 lines</td>
      <td align="center">189 lines</td>
    </tr>
  </tbody>
</table>


## Examples
Here are some examples:

Hello World
````ruby
print "Hello World"
````

Sum of two numbers
````ruby
print "Enter the first number:";
a := read;
print "Enter the second number:";
b := read;
sum := a + b;
print "The sum is: ";
print sum
````

Fibonacci Sequence
````ruby
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

The formal syntax is as follows (ANTLR syntax):

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

## Compiling & Running

To compile you need to install [sbt](https://www.scala-sbt.org/). The easiest way is to use [Sdkman](https://sdkman.io/install) (Linux) or [Scoop](https://scoop.sh/) (Windows).

````shell
$ sbt
sbt> clean
sbt> compile

# To run the interpreter
sbt> runMain whilelang.interpreter.main sum.while

# To run the transpiler
sbt> runMain whilelang.compiler.main sum.while
````
