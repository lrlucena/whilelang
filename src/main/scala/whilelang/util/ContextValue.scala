package whilelang.util

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty as Property}

trait ContextValue:
  given Property[Any] = Property[Any]()

  extension[A](tree: ParseTree)(using values: Property[A])
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: A) = values.put(tree, v)
