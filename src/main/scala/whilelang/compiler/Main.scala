package whilelang.compiler

import whilelang.parser.Runner

@main def main(file: String) =
  Runner.run(file)(p => println(p.translate))
