package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class VariableDeclarationExpression(
    override val position: SourcePosition,

    val identifier: String,

    val immutable: Boolean,

    val initializer: Expression
): Expression
