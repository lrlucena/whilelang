package whilelang.interpreter

import whilelang.util.Runner.run

@main def main(file: String) = run(file)(_.execute)
