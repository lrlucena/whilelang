package whilelang.compiler

import whilelang.util.Runner

val action = Runner(program => println(program.meaning))

@main def main(file: String) = action(file)
