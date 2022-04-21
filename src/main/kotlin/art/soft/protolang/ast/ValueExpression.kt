package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class ValueExpression(
    override val position: SourcePosition,

    val value: Any,

    override val isOptional: Boolean = false
): Expression {

    override fun toString() = value.toString()
}
