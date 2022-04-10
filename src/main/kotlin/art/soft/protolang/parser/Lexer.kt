package art.soft.protolang.parser

import art.soft.protolang.config.CompilerOptions
import art.soft.protolang.exceptions.LexerException
import java.lang.StringBuilder

class Lexer(
    source: String? = null,

    filename: String? = null,

    private val options: CompilerOptions = CompilerOptions.DEFAULT,

    private val input: CharIterator = iteratorFromSource(source, filename, options),

    private val collectComments: Boolean = false
) {
    companion object {
        private const val ALLOWED_WHITESPACES = " "

        private const val NEW_LINE_CHARS = "\n"

        private const val OPERATOR_CHARS = "@+-*/%()[]{}=<>!&|.,^?:;"

        private val OPERATORS = TokenType.toMapByType(TokenType.Type.OPERATOR)

        private val KEYWORDS = TokenType.toMapByType(TokenType.Type.KEYWORD)

        private val UNDERSCORE_REGEX = Regex("\\_{2,}")

        private fun iteratorFromSource(source: String?, filename: String?, options: CompilerOptions): CharIterator {
            return source?.iterator() ?: "".iterator()
        }

        val EOF_TOKEN = Token(TokenType.EOF, "", Position(null, null, -1, -1, -1, -1, -1))
    }

    private var pos: Int = 0

    private val tokens: MutableList<Token> = mutableListOf()

    private val buffer: StringBuilder = StringBuilder()

    private var currentLineIndex = 0

    private val errors = mutableListOf<Error>()

    private var row = 1
    private var col = 1

    private var peekc: Char = '\u0000'

    private var position = Position(source = source, filename = filename)

    private var isFatal = false

    fun tokenize(): List<Token> {
        pos--
        col--
        next()
        val indent = trySkipIndentation()
        position = position.copy(indent = indent, row = row, col = col)

        while (hasNext()) tokenizeAtom()

        if (errors.isNotEmpty()) throw LexerException("LexerException", errors)

        return tokens
    }

    private fun tokenizeAtom() {
        position = getCurrentPosition()
        val current = peek()
        when {
            current.isDigit() -> tokenizeNumber()
            current.isIdentifierStart() -> tokenizeIdentifier()
            current == '`' -> tokenizeExtendedIdentifier()
            current == '#' -> tokenizeComment()
            current == '\"' || current == '\'' -> tokenizeString(current)
            current == '$' -> {
                val nextChar = next()
                if (nextChar == '\'' || nextChar == '\"') tokenizeString(nextChar, true)
                else addToken(TokenType.DOLLAR)
            }
            current in OPERATOR_CHARS -> tokenizeOperator()
            current in ALLOWED_WHITESPACES -> next()
            current in NEW_LINE_CHARS -> insertNewLine()
            else -> error("Unknown character '$current'")
        }
    }

    private fun tokenizeNumber() {
        clearBuffer()
        val current = peek()
        buffer.append(current)
        if (current == '0') {
            when (next()) {
                'x' -> tokenizeHexNumber()
                'o' -> tokenizeOctNumber()
                'b' -> tokenizeBinNumber()
                else -> tokenizeDecNumber()
            }
        } else {
            next()
            tokenizeDecNumber()
        }
    }

    private fun tokenizeHexNumber() {
        clearBuffer()
        var current = next()
        while (current.isHexNumber() || current == '_') {
            buffer.append(current)
            current = next()
        }
        if (buffer.isNotEmpty()) {
            addToken(TokenType.NUMBER_INTEGER_HEX, formatNumberString(buffer.toString(), "0x"))
        } else {
            error("Invalid hex number format digits excepted after \"0x\"")
        }
    }

    private fun tokenizeOctNumber() {
        clearBuffer()
        var current = next()
        while (current.isOctNumber() || current == '_') {
            buffer.append(current)
            current = next()
        }
        if (buffer.isNotEmpty()) {
            addToken(TokenType.NUMBER_INTEGER_OCT, formatNumberString(buffer.toString(), "0o"))
        } else {
            error("Invalid oct number format digits excepted after \"0o\"")
        }
    }

    private fun tokenizeBinNumber() {
        clearBuffer()
        var current = next()
        while (current.isBinNumber() || current == '_') {
            buffer.append(current)
            current = next()
        }
        if (buffer.isNotEmpty()) {
            addToken(TokenType.NUMBER_INTEGER_BIN, formatNumberString(buffer.toString(), "0b"))
        } else {
            error("Invalid bin number format digits excepted after \"0b\"")
        }
    }

    private fun tokenizeDecNumber() {
        tokenizeFloatPart(readNumber())
    }

    private fun tokenizeFloatPart(number: String) {
        when (peek()) {
            '.' -> readFloatDotPart(number)
            'e', 'E' -> readFloatEpsilonPart(number)
            else -> addToken(TokenType.NUMBER_INTEGER, number)
        }
    }

    private fun readFloatDotPart(number: String) {
        when (next()) {
            in '0'..'9' -> {
                val floatNumber = number + '.' + readNumber()
                when (peek()) {
                    'e', 'E' -> readFloatEpsilonPart(floatNumber)
                    else -> addToken(TokenType.NUMBER_FLOAT, floatNumber)
                }
            }
            '.' -> {
                addToken(TokenType.NUMBER_INTEGER, number)
                addToken(if (next() == '.') TokenType.DOT_DOT_DOT else TokenType.RANGE)
            }
            else -> {
                addToken(TokenType.NUMBER_INTEGER, number)
                addToken(TokenType.DOT)
            }
        }
    }

    private fun readFloatEpsilonPart(number: String) {
        var signChar = next()
        if (signChar != '-' && signChar != '+') {
            signChar = '+'
        } else {
            next()
        }
        addToken(TokenType.NUMBER_FLOAT, number + 'e' + signChar + readNumber())
    }

    private fun readNumber(): String {
        var current = peek()
        while (current.isDigit() || current == '_') {
            buffer.append(current)
            current = next()
        }
        val number = formatNumberString(buffer.toString())
        clearBuffer()
        return if (number.isEmpty()) {
            error("Invalid number format digits excepted")
            "0"
        } else {
            number
        }
    }

    private fun formatNumberString(number: String, prefix: String = ""): String {
        if (number.startsWith('_') || number.endsWith('_') || number.matches(UNDERSCORE_REGEX)) {
            error("Incorrect position of '_' symbol in number: $prefix$number")
        }
        return number.replace("_", "")
    }

    private fun Char.isHexNumber() = isDigit() || this in 'a'..'f' || this in 'A'..'F'

    private fun Char.isOctNumber() = this in '0'..'7'

    private fun Char.isBinNumber() = this in '0'..'1'

    private fun tokenizeIdentifier() {
        clearBuffer()
        buffer.append(peek())
        var current = next()
        while (true) {
            if (!current.isIdentifierPart()) {
                break
            }
            buffer.append(current)
            current = next()
        }

        val word = buffer.toString()
        val keyword = KEYWORDS[word]
        if (keyword != null) {
            addToken(keyword, keyword.value)
        } else {
            addToken(TokenType.IDENTIFIER, word)
        }
    }

    private fun tokenizeExtendedIdentifier() {
        next() // skip open `

        clearBuffer()
        var current = peek()
        while (true) {
            if (current == '`') break
            if (current == '\u0000' || current == '\n' || current == '\r') {
                error("Reached end of line while parsing extended word")
                break
            }
            buffer.append(current)
            current = next()
        }
        if (current == '`') next() // skip closing `

        addToken(TokenType.IDENTIFIER_LITERAL, buffer.toString())
    }

    private fun tokenizeComment() {
        clearBuffer()
        while (true) {
            val char = next()
            if (!hasNext() || char in NEW_LINE_CHARS) break
            if (collectComments) buffer.append(char)
        }
        if (collectComments) {
            addToken(TokenType.COMMENT, buffer.toString().trim())
        }
    }

    private fun tokenizeString(openChar: Char, interpolated: Boolean = false) {
        clearBuffer()
        val nextChar = next() // skip start character
        if (nextChar == openChar) {
            if (next() != openChar) addToken(TokenType.STRING) // Empty string
            else tokenizeRawString(openChar)
            return
        }
        while (true) {
            val current = peek()
            when (current) {
                openChar -> break
                '\n' -> {
                    error("Not allowed new line in string literal", getCurrentPosition())
                    insertNewLine()
                    break
                }
                '{' -> if (interpolated) {
                    tokenizeInterpolatedStringValue()
                    continue
                }
                '\u0000' -> {
                    error("Reached end of source code while parsing text string")
                    break
                }
            }
            buffer.append(if (current == '\\') charEscape() else current)
            next()
        }
        if (peek() == openChar) next() // skip closing character

        addToken(TokenType.STRING, buffer.toString())
    }

    private fun tokenizeRawString(openChar: Char, interpolated: Boolean = false) {
        val indentation = options.indentation.repeat(position.indent)
        val nextChar = next() // skip open raw string
        if (nextChar == '\n') insertRawStringNewLine(indentation)
        while (true) {
            val current = peek()
            when (current) {
                openChar -> {
                    if (next() == openChar) {
                        // End of raw string
                        if (next() == openChar) break
                        else buffer.append(openChar)
                    }
                    buffer.append(openChar)
                    continue
                }
                '{' -> if (interpolated) {
                    tokenizeInterpolatedStringValue()
                    continue
                }
                '\n' -> {
                    insertRawStringNewLine(indentation)
                    buffer.append('\n')
                    continue
                }
                '\u0000' -> {
                    error("Reached end of source code while parsing text string")
                    break
                }
            }
            buffer.append(current)
            next()
        }
        if (peek() == openChar) next() // skip closing character

        if (buffer.endsWith('\n')) buffer.setLength(buffer.length - 1)

        addToken(TokenType.STRING, buffer.toString())
    }

    private fun insertRawStringNewLine(indentation: String) {
        setCurrentLineEnd(pos)
        if (next() == '\r') next()
        row++
        col = 1
        for (spaceChar in indentation) {
            if (peek() != spaceChar) {
                error("Invalid indentation in raw string literal", getCurrentPosition())
                break
            }
            next()
        }
    }

    private fun tokenizeInterpolatedStringValue() {
        addToken(TokenType.STRING, buffer.toString())
        position = getCurrentPosition()
        addToken(TokenType.INTERPOLATION_START)
        next() // skip '{'
        var currentTokenIndex = tokens.size
        var openBraceCount = 0
        while (hasNext()) {
            tokenizeAtom()
            if (tokens.size != currentTokenIndex) {
                val lastToken = tokens.last()
                when (lastToken.type) {
                    TokenType.LBRACE -> openBraceCount++
                    TokenType.RBRACE -> {
                        if (openBraceCount == 0) {
                            tokens.removeLast()
                            tokens.add(Token(
                                type = TokenType.INTERPOLATION_END,
                                position = lastToken.position
                            ))
                            break
                        } else openBraceCount--
                    }
                }
                currentTokenIndex = tokens.size
            }
        }
        position = getCurrentPosition()
        clearBuffer()
    }

    private fun charEscape(): Char {
        return when (val nextChar = next()) {
            '0' -> '\u0000'
            'b' -> '\b'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            '\n' -> {
                error("Not allowed new line in string literal", getCurrentPosition())
                insertNewLine()
                '\n'
            }
            'x' -> readHexCharacter(2)
            'u' -> readHexCharacter(4)
            else -> nextChar
        }
    }

    private fun readHexCharacter(count: Int): Char {
        var value = 0
        for (i in (count * 4) downTo 0 step 4) {
            val digit = next()
            if (!digit.isHexNumber()) {
                error(
                    "Invalid hex representation of number. Must contain $count hex digit",
                    getCurrentPosition()
                )
                break
            }
            value += Character.digit(digit, 16) shr i
        }
        return value.toChar()
    }

    private fun tokenizeOperator() {
        var current = peek()
        var text = ""
        while (true) {
            val nextText = text + current
            if (text.isNotEmpty() && !OPERATORS.containsKey(nextText)) {
                val operator = OPERATORS[text]!!
                addToken(operator, operator.value)
                return
            }
            text = nextText
            current = next()
        }
    }

    private fun getCurrentPosition() = position.copy(row = row, col = col)

    private fun Char.isIdentifierStart(): Boolean =
        this.isLetter() || this == '_'

    private fun Char.isIdentifierPart(): Boolean =
        this.isLetterOrDigit() || this == '_'

    private fun trySkipIndentation(): Int {
        var indent = 0

        while (true) {
            for ((index, spaceChar) in options.indentation.withIndex()) {
                if (peek() != spaceChar) {
                    if (index == 0) return indent
                    error("Incorrect indentation")
                    return indent
                }
                next()
            }
            indent += 1
        }
    }

    private fun clearBuffer() {
        buffer.setLength(0)
    }

    private fun insertNewLine() {
        setCurrentLineEnd(pos)
        if (next() == '\r') next()
        row++
        col = 1
        val indent = trySkipIndentation()
        position = position.copy(lineStart = pos, row = row, col = col, indent = indent)
    }

    private fun setCurrentLineEnd(lineEnd: Int) {
        if (currentLineIndex != tokens.size) {
            for (index in currentLineIndex until tokens.size) {
                (tokens[index].position as Position).lineEnd = lineEnd
            }
            currentLineIndex = tokens.size
        }
    }

    private fun peek() = peekc

    private fun next(): Char {
        pos++
        col++
        peekc = if (input.hasNext()) input.next() else '\u0000'
        return peekc
    }

    private fun hasNext() = peekc != '\u0000'

    private fun addToken(type: TokenType, value: String = "") {
        tokens.add(Token(type, value, position))
    }

    private fun error(message: String, position: SourcePosition = this.position, fatal: Boolean = false) {
        if (!isFatal) {
            errors += Error(message, position)
            isFatal = fatal
        }
    }

    data class Position(
        override val filename: String?,
        override val source: String?,
        override val row: Int = 0,
        override val col: Int = 0,
        override val indent: Int = 0,
        override val lineStart: Int = 0,
        override var lineEnd: Int = 0
    ) : SourcePosition {

        override fun getErrorMessage(message: String, options: CompilerOptions) = buildString {
            append("at line ").append(toString()).append('\n')
            if (source != null) {
                append(source.substring(lineStart, lineEnd)).append('\n')
                append(" ".repeat(col)).append("^\n")
            }
            append(message)
        }

        override fun toString() = "[${row}:${col}]"
    }

    data class Error(val message: String, val position: SourcePosition)
}