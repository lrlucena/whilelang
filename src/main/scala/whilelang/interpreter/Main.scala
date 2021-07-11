package whilelang.interpreter

import whilelang.parser.Runner

@main def main(file: String) = Runner.run(file)(_.execute)
