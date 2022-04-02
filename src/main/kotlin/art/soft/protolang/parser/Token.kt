package art.soft.protolang.parser

data class Token(
    private val type: TokenType,
    private val text: String?,
    private val position: SourcePosition
) {
    fun position() = position.toString()

    override fun toString() = "$type $position $text"
}
