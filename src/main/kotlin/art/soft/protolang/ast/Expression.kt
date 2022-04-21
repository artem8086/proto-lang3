package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

interface Expression {

    val position: SourcePosition

    val isOptional: Boolean get() = false
}