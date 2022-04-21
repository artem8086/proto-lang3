package art.soft.protolang.parser

abstract class Scanner(
    private val tokens: List<Token>
) {
    private var pos = 0

    protected abstract fun error(message: String)

    protected fun getErrorLine(): SourcePosition {
        if (tokens.isEmpty()) return Lexer.EOF_TOKEN.position
        return if (pos >= tokens.size) tokens.last().position else tokens[pos].position
    }

    private var indent: Int = 0

    private var row: Int = 1

    protected fun tokenOnNextLine(pos: Int = 0): Boolean {
        return get(pos - 1).position.row < get(pos).position.row
    }

    protected fun nextIndent(index: Int = 0): Int {
        val oldIndent = indent
        val position = get(index).position
        indent = position.indent
        row = position.row
        return oldIndent
    }

    protected fun checkIndent(): Boolean {
        val position = get().position
        if (row < 0) return false
        if (position.row == row) return true
        val tokenIndent = position.indent + 1
        if (tokenIndent > indent) {
            error("Indentation error, too much indent")
        }
        return indent == tokenIndent
    }

    protected fun checkIndentOr(oldIndent: Int): Boolean {
        val position = get().position
        if (position.row == row) return true
        val tokenIndent = position.indent
        if (tokenIndent == oldIndent) return true
        if (tokenIndent > indent) {
            error("Indentation error, too much indent")
        }
        return indent == tokenIndent
    }

    protected fun consume(type: TokenType): Token {
        val current = get()
        if (type != current.type) {
            throw RuntimeException("Token $current doesn't match $type")
        }
        pos++
        return current
    }

    protected fun consumeWord(): Token {
        val current = get()
        if (TokenType.IDENTIFIER != current.type || TokenType.IDENTIFIER_LITERAL != current.type) {
            throw RuntimeException("Token $current doesn't match identifier")
        }
        pos++
        return current
    }

    protected fun matchWord(): Token? {
        val current = get()
        if (current.type == TokenType.IDENTIFIER || current.type == TokenType.IDENTIFIER_LITERAL) {
            pos++
            return current
        }
        return null
    }

    protected fun matchIdentifier(): Token? {
        val current = get()
        if (current.type.isIdentifier) {
            pos++
            return current
        }
        return null
    }

    protected fun match(type: TokenType): Boolean {
        if (type != get().type) {
            return false
        }
        pos++
        return true
    }

    protected fun prevSourcePos() = get(-1).position

    protected fun sourcePos() = get().position

    protected fun skip(value: Int = 1) {
        pos += value
    }

    protected fun skip(type: TokenType) {
        if (checkIndent() && match(type)) pos++
    }

    protected fun match2(type1: TokenType, type2: TokenType): Boolean {
        if (type1 != get().type) {
            return false
        }
        if (type2 != get(1).type) {
            return false
        }
        pos += 2
        return true
    }

    protected fun lookMatchWord(row: Int, pos: Int = 0): Boolean {
        val type = this[pos].type
        return type == TokenType.IDENTIFIER || type == TokenType.IDENTIFIER_LITERAL
    }

    protected fun lookMatch(row: Int, type: TokenType, pos: Int = 0): Boolean {
        val current = get(pos)
        return current.type == type && current.position.row == row
    }

    protected operator fun get(relativePosition: Int = 0): Token {
        val position = pos + relativePosition
        return if (position >= tokens.size) Lexer.EOF_TOKEN else tokens[position]
    }
}