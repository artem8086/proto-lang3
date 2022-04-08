package art.soft.protolang.parser

data class Token(
    val type: TokenType,
    val value: String = "",
    val position: SourcePosition
) {
    override fun toString() = "$type $position $value"
}
