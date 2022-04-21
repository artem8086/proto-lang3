package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class TupleExpression(
    override val position: SourcePosition,

    val expressions: List<Expression>
): Expression
