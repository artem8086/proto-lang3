package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class TypeCheckExpression(
    override val position: SourcePosition,

    val expression: Expression,

    val typeExpression: Expression,

    override val isOptional: Boolean = false
) : Expression
