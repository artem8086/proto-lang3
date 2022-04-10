package art.soft.protolang.parser

import art.soft.protolang.config.CompilerOptions

class Parser(
    private val tokens: List<Token>,

    private val moduleName: String?,

    private val options: CompilerOptions = CompilerOptions.DEFAULT
) {
    private var pos = 0

    private val parseErrors = mutableListOf<Error>()

    private val warnings = mutableListOf<Warning>()

    private fun getErrorLine(): SourcePosition {
        if (tokens.isEmpty()) return Lexer.EOF_TOKEN.position
        return if (pos >= tokens.size) tokens.last().position else tokens[pos].position
    }

    private fun warning(message: String, token: Token) {
        if (options.parserWarningsEnable) {
            warnings += Warning(message, token.position)
        }
    }

    private fun error(message: String) {
        parseErrors += Error(RuntimeException(message), getErrorLine())
    }

    private fun consume(type: TokenType): Token {
        val current = get()
        if (type !== current.type) {
            throw RuntimeException("Token $current doesn't match $type")
        }
        pos++
        return current
    }

    private fun matchIdentifier(): Boolean {
        if (get().type.isIdentifier) {
            pos++
            return true
        }
        return false
    }

    private fun match(type: TokenType): Boolean {
        if (type !== get().type) {
            return false
        }
        pos++
        return true
    }

    private fun match2(type1: TokenType, type2: TokenType): Boolean {
        if (type1 !== get().type) {
            return false
        }
        if (type2 !== get(1).type) {
            return false
        }
        pos += 2
        return true
    }

    private fun lookMatch(type: TokenType, pos: Int = 0) = this[pos].type == type

    private operator fun get(relativePosition: Int = 0): Token {
        val position = pos + relativePosition
        return if (position >= tokens.size) Lexer.EOF_TOKEN else tokens[position]
    }

    data class Error(val exception: Exception, val position: SourcePosition)

    data class Warning(val message: String, val position: SourcePosition) {
        override fun toString() = "Warning on $position: $message"
    }
}