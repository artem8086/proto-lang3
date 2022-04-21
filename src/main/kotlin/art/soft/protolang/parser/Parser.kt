package art.soft.protolang.parser

import art.soft.protolang.ast.*
import art.soft.protolang.config.CompilerOptions
import art.soft.protolang.parser.TokenType.*

class Parser(
    tokens: List<Token>,

    private val options: CompilerOptions = CompilerOptions.DEFAULT
): Scanner(tokens) {
    private val parseErrors = mutableListOf<Error>()

    private val warnings = mutableListOf<Warning>()

    fun expression(): Expression {
        return nullCoalesce()
    }

    private inline fun methodCallConstruct(
        identifier: String,
        isOptional: Boolean = false,
        vararg arguments: Expression,
        context: () -> Expression
    ): MethodCallExpression {

        val sourcePos = prevSourcePos()
        val ctx = context()
        val fieldRef = FieldRefExpression(sourcePos, ctx, identifier, isOptional)
        return MethodCallExpression(sourcePos, fieldRef, arguments.toList())
    }

    private inline fun constructExpression(vararg types: TokenType, baseExpression: () -> Expression): Expression {
        var result = baseExpression()
        var isMatch = true

        while (isMatch) {
            isMatch = false
            if (checkIndent()) {
                for (type in types) {
                    if (match(type)) {
                        result = methodCallConstruct(type.value, checkIndent() && match(QUESTION), baseExpression()) { result }
                        isMatch = true
                    }
                }
            }
        }

        return result
    }

    private fun nullCoalesce(): Expression {
        var result = infixOperator()

        while (true) {
            if (checkIndent() && match(NONE_COALESCE)) {
                result = NoneCoalesceExpression(prevSourcePos(), result, expression())
                continue
            }
            break;
        }

        return result
    }

    private fun infixOperator() = constructExpression(IDENTIFIER, IDENTIFIER_LITERAL) { logicalOr() }

    private fun logicalOr() = constructExpression(OR) { logicalAnd() }

    private fun logicalAnd() = constructExpression(AND) { equality() }

    private fun equality() = constructExpression(EQ, NE) { conditional() }

    private fun conditional() = constructExpression(LT, LTE, GT, GTE) { shift() }

    private fun shift() = constructExpression(SHIFT_L, SHIFT_R) { additive() }

    private fun additive() = constructExpression(PLUS, MINUS) { multiplicative() }

    private fun multiplicative() = constructExpression(MUL, DIV, MOD, POW) { unary() }

    private fun unary(): Expression {
        if (checkIndent()) {
            if (match(PLUS)) {
                return methodCallConstruct(PLUS.value) { primary() }
            }
            if (match(MINUS)) {
                return methodCallConstruct(MINUS.value) { primary() }
            }
            if (match(NOT)) {
                methodCallConstruct(NOT.value) { primary() }
            }
            if (match(MUL)) {
                return VarArgsUnpackExpression(prevSourcePos(), primary())
            }
            if (match(REFERENCE)) {
                val sourcePos = prevSourcePos()
                val expression = expression()
                return if (expression is MethodCallExpression) {
                    MethodRefExpression(sourcePos, expression)
                } else {
                    error("Reference expression must be method call expression")
                    ValueExpression(sourcePos, UndefinedValue())
                }
            }
            lambda()?.let { return it }
        }
        return primary()
    }

    private fun primary(): Expression {
        if (checkIndent()) {
            if (match(LPAREN)) {
                val sourcePos = prevSourcePos()
                val expressions = collectIndentScope(SEMICOLON, RPAREN) { expression() }
                return if (expressions.size == 1) {
                    expressions.first()
                } else {
                    SequenceExpression(sourcePos, expressions)
                }
            }
            if (match(LBRACKET)) {
                val sourcePos = prevSourcePos()
                // Open indentation scope
                val indent = nextIndent()
                val expressions = collectIndentScope(COMMA, RBRACKET) { expression() }
                TODO("Implement array collector")
            }
        }
        return variable()
    }

    private fun variable(): Expression {
        val token = get()
        skip()
        return when (token.type) {
            STRING -> ValueExpression(token.position, token.value)
            BOOL_TRUE -> ValueExpression(token.position, true)
            BOOL_FALSE -> ValueExpression(token.position, false)
            IDENTIFIER, IDENTIFIER_LITERAL -> VariableRefExpression(token.position, token.value)
            THIS -> ValueExpression(token.position, ThisValue())
            FIELD -> ValueExpression(token.position, FieldValue())
            NUMBER_INTEGER -> ValueExpression(token.position, token.value.toLong())
            NUMBER_INTEGER_BIN -> ValueExpression(token.position, token.value.toLong(2))
            NUMBER_INTEGER_OCT -> ValueExpression(token.position, token.value.toLong(8))
            NUMBER_INTEGER_HEX -> ValueExpression(token.position, token.value.toLong(16))
            NUMBER_FLOAT -> ValueExpression(token.position, token.value.toDouble())
            else -> {
                if (checkIndent()) {
                    skip(-1)
                } else {
                    error("Unexpected token $token")
                }
                ValueExpression(token.position, UndefinedValue())
            }
        }
    }

    private fun lambda(): FunctionExpression? {
        var index = 0
        val currentRow = get().position.row
        while (true) {
            if (!lookMatchWord(currentRow, index)) break
            index++
            if (lookMatch(currentRow, COLON)) return parseLambda()
            if (!lookMatch(currentRow, COMMA, index)) break
            index++
        }
        return null
    }

    private fun parseLambda(): FunctionExpression {
        var index = 0
        val sourcePos = sourcePos()
        val arguments = mutableListOf<FunctionExpression.Argument>()
        do {
            val argument = consumeWord()
            arguments += FunctionExpression.Argument(argument.position, argument.value)
        } while (match(COMMA))
        consume(COLON)
        return FunctionExpression(sourcePos, arguments, collectIndentScopeExpressions(SEMICOLON))
    }

    private fun collectIndentScopeExpressions(skipToken: TokenType, closeToken: TokenType? = null): Expression {
        val sourcePos = prevSourcePos()
        return if (tokenOnNextLine()) {
            val expressions = collectIndentScope(skipToken, closeToken) { expression() }
            if (expressions.size == 1) {
                expressions.first()
            } else {
                SequenceExpression(sourcePos, expressions)
            }
        } else expression()
    }

    private inline fun <T> collectIndentScope(skipToken: TokenType, closeToken: TokenType? = null, collector: () -> T): List<T> {
        // Open indentation scope
        val indent = nextIndent()
        val collection = mutableListOf<T>()
        while (true) {
            collection += collector()

            if (
                (closeToken != null && checkIndentOr(indent) && match(closeToken)) || !checkIndent()
            ) {
                // Close indentation scope
                nextIndent(-1)
                return collection
            }
            skip(skipToken)
        }
    }

    private fun warning(message: String, token: Token) {
        if (options.parserWarningsEnable) {
            warnings += Warning(message, token.position)
        }
    }

    override fun error(message: String) {
        parseErrors += Error(RuntimeException(message), getErrorLine())
    }

    data class Error(val exception: Exception, val position: SourcePosition)

    data class Warning(val message: String, val position: SourcePosition) {
        override fun toString() = "Warning on $position: $message"
    }
}