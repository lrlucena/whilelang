package whilelang.util

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.{ParseTree, ParseTreeProperty as Property}
import scala.jdk.CollectionConverters.ListHasAsScala

trait ContextValue:
  given Property[Any] = Property[Any]()

  extension(tree: ParseTree)(using values: Property[Any])
    def apply(i: Int) = tree.getChild(i)
    def text = tree.getText
    def value[E <: Any]: E = values.get(tree).asInstanceOf[E]
    def value_=(v: Any): Unit = values.put(tree, v)

  extension[E] (list: java.util.List[E])
    def map[T](f: E => T): Seq[T] = list.asScala.toSeq.map(f)
