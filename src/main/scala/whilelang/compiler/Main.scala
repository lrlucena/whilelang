package whilelang.compiler

import whilelang.util.Runner.run

@main def main(file: String) = run(file)(p => println(p.translate))
