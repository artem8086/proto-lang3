package art.soft.protolang.parser

data class Token(
    val type: TokenType,
    val value: String?,
    val position: SourcePosition
) {
    fun position() = position.toString()

    override fun toString() = "$type $position $value"
}
