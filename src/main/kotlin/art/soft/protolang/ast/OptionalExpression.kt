package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class OptionalExpression(
    override val position: SourcePosition

): Expression {
    override val isOptional: Boolean get() = true
}
