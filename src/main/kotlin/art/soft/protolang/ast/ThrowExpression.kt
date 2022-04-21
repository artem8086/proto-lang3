package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class ThrowExpression(
    override val position: SourcePosition,

    val expression: Expression
): Expression