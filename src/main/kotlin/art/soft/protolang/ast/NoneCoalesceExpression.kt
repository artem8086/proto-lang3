package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class NoneCoalesceExpression(
    override val position: SourcePosition,

    val optionalExpression: Expression,

    val ifEmptyExpression: Expression
): Expression
