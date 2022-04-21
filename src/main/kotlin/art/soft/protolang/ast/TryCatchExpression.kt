package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class TryCatchExpression(
    override val position: SourcePosition,

    val tryExpression: Expression,

    val catchExpression: Expression,

    val finallyExpression: Expression
): Expression