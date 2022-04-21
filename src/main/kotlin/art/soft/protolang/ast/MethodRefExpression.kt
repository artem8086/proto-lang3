package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class MethodRefExpression(
    override val position: SourcePosition,

    val methodCall: MethodCallExpression
): Expression
