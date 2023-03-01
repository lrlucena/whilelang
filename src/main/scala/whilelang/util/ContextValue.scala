package whilelang.util

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}

trait ContextValue:
  given ParseTreeProperty[Any] = ParseTreeProperty[Any]()

  extension (tree: ParseTree)(using values: ParseTreeProperty[Any])
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: Any) = values.put(tree, v)
