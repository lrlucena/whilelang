package whilelang.util

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}

trait Antlr2Scala[T]:
  private[Antlr2Scala] val values = ParseTreeProperty[T]

  given Conversion[ParseTree, Tree2Scala] = Tree2Scala(_)
  private[Antlr2Scala] case class Tree2Scala(tree: ParseTree):
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: T) = values.put(tree, v)
