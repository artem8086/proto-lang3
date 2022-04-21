package art.soft.protolang.ast

import art.soft.protolang.parser.SourcePosition

data class ObjectExpression(
    override val position: SourcePosition,

    val extensions: List<Expression>,

    val fields: List<Field>,

    val innerObjects: List<ObjectExpression>
): Expression {

    data class Field(
        val name: String,

        val function: FunctionExpression,

        val isSetter: Boolean = false
    )
}
