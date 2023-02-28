package whilelang.interpreter

import whilelang.util.Runner

def action = Runner(program => program.execute)

@main def main(file: String) = action(file)
