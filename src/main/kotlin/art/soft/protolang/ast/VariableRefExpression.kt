package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class VariableRefExpression(
    override val position: SourcePosition,

    val identifier: String
): Expression {

    override fun toString() = identifier
}
