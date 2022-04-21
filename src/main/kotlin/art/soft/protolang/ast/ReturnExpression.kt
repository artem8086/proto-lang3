package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class ReturnExpression(
    override val position: SourcePosition,

    val expression: Expression
): Expression
