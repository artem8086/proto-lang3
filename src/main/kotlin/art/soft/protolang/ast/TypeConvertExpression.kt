package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class TypeConvertExpression(
    override val position: SourcePosition,

    val expression: Expression,

    val typeException: Expression,

    override val isOptional: Boolean = false
): Expression