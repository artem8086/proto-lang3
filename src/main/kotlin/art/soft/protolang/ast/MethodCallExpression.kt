package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class MethodCallExpression(
    override val position: SourcePosition,

    val context: Expression,

    val arguments: List<Expression> = emptyList(),

    val namedArguments: List<NamedArgument> = emptyList(),

    val setter: Boolean = false
): Expression {

    data class NamedArgument(val name: String, val expression: Expression) {
        override fun toString() = "$name = $expression"
    }

    override fun toString() = "$context(${(arguments.map { it.toString() } + namedArguments.map { it.toString() }).joinToString(", ")})"
}
