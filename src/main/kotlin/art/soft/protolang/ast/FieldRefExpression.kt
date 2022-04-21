package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class FieldRefExpression(
    override val position: SourcePosition,

    val context: Expression,

    val identifier: String,

    val optional: Boolean = false
): Expression {

    override fun toString() = "$context.$identifier"
}
