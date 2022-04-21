package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class ContinueExpression(override val position: SourcePosition): Expression