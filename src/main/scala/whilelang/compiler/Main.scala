package whilelang.compiler

import whilelang.util.Runner

def action = Runner.run(program => println(program.translate))

@main def main(file: String) = action(file)
