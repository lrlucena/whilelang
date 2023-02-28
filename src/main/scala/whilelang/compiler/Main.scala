package whilelang.compiler

import whilelang.util.Runner

def action = Runner(program => println(program.meaning))

@main def main(file: String) = action(file)
