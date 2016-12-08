package whilelang

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeProperty

trait Antlr2Scala {
  protected val values = new ParseTreeProperty[Any]
  protected implicit class rule2scala(rule: ParserRuleContext) {
    def apply(i: Int) = rule.getChild(i)
    def value[T]: T = values.get(rule).asInstanceOf[T]
    def value_=(v: Any) = values.put(rule, v)
  }
  protected implicit class tree2scala(tree: ParseTree) {
    def text = tree.getText
  }
}
