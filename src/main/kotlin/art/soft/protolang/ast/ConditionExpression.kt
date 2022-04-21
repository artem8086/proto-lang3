package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class ConditionExpression(
    override val position: SourcePosition,

    val condition: Expression,

    val thenExpression: Expression,

    val elseExpression: Expression
): Expression
