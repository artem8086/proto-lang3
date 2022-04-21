package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class FunctionExpression(
    override val position: SourcePosition,

    val arguments: List<Argument>,

    val body: Expression
): Expression {

    data class Argument(
        val position: SourcePosition,

        val name: String,

        val initial: Expression? = null,

        val isTypeParam: Boolean = false,

        val isWrapperParam: Boolean = false
    )
}
