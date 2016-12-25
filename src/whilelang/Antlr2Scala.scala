package whilelang

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}

trait Antlr2Scala {
  protected val values = new ParseTreeProperty[Any]
  protected implicit class tree2scala(tree: ParseTree) {
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[T]: T = values.get(tree).asInstanceOf[T]
    def value_=(v: Any) = values.put(tree, v)
  }
}
