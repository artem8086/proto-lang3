package art.soft.protolang.parser

import art.soft.protolang.parser.TokenType.Type.*

enum class TokenType(val value: String = "", val type: Type, val isOperator: Boolean = type == OPERATOR) {
    NUMBER_INTEGER(type = VALUE),
    NUMBER_INTEGER_BIN(type = VALUE),
    NUMBER_INTEGER_DEC(type = VALUE),
    NUMBER_INTEGER_OCT(type = VALUE),
    NUMBER_INTEGER_HEX(type = VALUE),
    NUMBER_FLOAT(type = VALUE),
    IDENTIFIER(type = VALUE),
    STRING(type = VALUE),
    REGEXP(type = VALUE),
    DOCUMENTATION(type = VALUE),
    COMMENT(type = VALUE),

    // Keywords
    BOOL_TRUE("true", KEYWORD),
    BOOL_FALSE("false", KEYWORD),
    NONE("none", KEYWORD),

    // Control flow
    IF("if", KEYWORD),
    ELSE("else", KEYWORD),
    ELIF("elif", KEYWORD),
    BREAK("break", KEYWORD),
    CONTINUE("continue", KEYWORD),
    RETURN("return", KEYWORD),

    // Cycles
    FOR("for", KEYWORD),
    WHILE("while", KEYWORD),

    // Exceptions
    TRY("try", KEYWORD),
    CATCH("catch", KEYWORD),
    FINALLY("finally", KEYWORD),
    THROW("throw", KEYWORD),

    // Enum declaration
    ENUM("enum", KEYWORD),

    // Variable declaration
    LET("let", KEYWORD),
    VAR("var", KEYWORD),
    TYPE("type", KEYWORD),

    THIS("this", KEYWORD),

    // Type operation
    IS("is", KEYWORD),
    AS("as", KEYWORD),

    // Futures
    YIELD("yield", KEYWORD),

    // Boolean operations
    NOT("not", KEYWORD),
    AND("and", KEYWORD),
    OR("or", KEYWORD),

    // Contains operator
    IN("in", KEYWORD, true),

    // Operators and punctuation
    PLUS("+", OPERATOR),
    MINUS("-", OPERATOR),
    MUL("*", OPERATOR),
    MOD("%", OPERATOR),
    POW("^", OPERATOR),

    SHIFT_R(">>", OPERATOR),
    SHIFT_L("<<", OPERATOR),

    REFERENCE("&", OPERATOR),

    PIPELINE("|", OPERATOR),

    ASSIGN("=", OPERATOR),

    EQ("==", OPERATOR),
    NE("!=", OPERATOR),
    LTE("<=", OPERATOR),
    GTE(">=", OPERATOR),
    LT("<", OPERATOR),
    GT(">", OPERATOR),

    QUESTION("?", OPERATOR),
    DOT(".", OPERATOR),
    COMMA(",", OPERATOR),
    RANGE("..", OPERATOR),
    DOT_DOT_DOT("...", OPERATOR),
    COLON(":", OPERATOR),
    SEMICOLON(";", OPERATOR),

    LPAREN("(", OPERATOR),
    RPAREN(")", OPERATOR),
    LBRACKET("[", OPERATOR),
    RBRACKET("]", OPERATOR),
    LBRACE("{", OPERATOR),
    RBRACE("}", OPERATOR),

    DECORATOR("@", OPERATOR);

    companion object {
        fun toMapByType(type: Type) = values().asSequence()
            .filter { it.type == type }
            .map { it.name to it }
            .toMap()
    }

    enum class Type {
        VALUE,
        KEYWORD,
        OPERATOR,
    }
}