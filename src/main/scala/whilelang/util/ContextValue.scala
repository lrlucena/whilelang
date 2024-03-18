package whilelang.util

import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty}
import scala.jdk.CollectionConverters.ListHasAsScala
import whilelang.parser.Element

trait ContextValue:
  private val values = ParseTreeProperty[Any]()

  extension (tree: ParseTree)
    def apply(i: Int): ParseTree = tree.getChild(i)
    def text: String = tree.getText
    def value[E <: Any]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: Any): Unit = values.put(tree, v)

  extension[E] (list: java.util.List[E])
    def map[T](f: E => T): Seq[T] = list.asScala.toSeq.map(f)
