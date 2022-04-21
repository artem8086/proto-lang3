package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class CycleExpression(
    override val position: SourcePosition,

    val condition: Expression,

    val expression: Expression
): Expression
