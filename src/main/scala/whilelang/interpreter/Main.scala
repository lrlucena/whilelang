package whilelang.interpreter

import whilelang.util.Runner

val action = Runner(program => program.execute())

@main def main(file: String) = action(file)
