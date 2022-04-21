package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class BreakExpression(override val position: SourcePosition): Expression
