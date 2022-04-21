package art.soft.protolang.parser

import art.soft.protolang.parser.TokenType.Type.*

enum class TokenType(val value: String = "", val type: Type, val isIdentifier: Boolean = type == OPERATOR) {
    NUMBER_INTEGER(type = VALUE, isIdentifier = true),
    NUMBER_INTEGER_BIN(type = VALUE),
    NUMBER_INTEGER_DEC(type = VALUE),
    NUMBER_INTEGER_OCT(type = VALUE),
    NUMBER_INTEGER_HEX(type = VALUE),
    NUMBER_FLOAT(type = VALUE),
    IDENTIFIER(type = VALUE, isIdentifier = true),
    IDENTIFIER_LITERAL(type = VALUE, isIdentifier = true),
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
    FIELD("field", KEYWORD),

    // Type operation
    IS("is", KEYWORD),
    AS("as", KEYWORD),

    // Futures
    YIELD("yield", KEYWORD),

    // Boolean operations
    NOT("not", KEYWORD, true),
    AND("and", KEYWORD, true),
    OR("or", KEYWORD, true),

    // Contains operator
    IN("in", KEYWORD, true),

    // Operators and punctuation
    PLUS("+", OPERATOR),
    MINUS("-", OPERATOR),
    MUL("*", OPERATOR),
    DIV("/", OPERATOR),
    MOD("%", OPERATOR),
    POW("^", OPERATOR),

    SHIFT_R(">>", OPERATOR),
    SHIFT_L("<<", OPERATOR),

    REFERENCE("&", OPERATOR, false),

    PIPELINE("|", OPERATOR, false),

    ASSIGN("=", OPERATOR, false),

    EQ("==", OPERATOR),
    NE("!=", OPERATOR),
    LTE("<=", OPERATOR),
    GTE(">=", OPERATOR),
    LT("<", OPERATOR),
    GT(">", OPERATOR),

    RANGE("..", OPERATOR),

    QUESTION("?", OPERATOR, false),
    NONE_COALESCE("??", OPERATOR, false),
    DOT(".", OPERATOR, false),
    COMMA(",", OPERATOR, false),
    DOT_DOT_DOT("...", OPERATOR, false),
    COLON(":", OPERATOR, false),
    SEMICOLON(";", OPERATOR, false),

    LPAREN("(", OPERATOR, false),
    RPAREN(")", OPERATOR, false),
    LBRACKET("[", OPERATOR, false),
    RBRACKET("]", OPERATOR, false),
    LBRACE("{", OPERATOR, false),
    RBRACE("}", OPERATOR, false),

    DOLLAR("$", OPERATOR, false),

    DECORATOR("@", OPERATOR, false),

    INTERPOLATION_START(type = OTHER),
    INTERPOLATION_END(type = OTHER),

    EOF("eof", OTHER);

    companion object {
        fun toMapByType(type: Type) = values().asSequence()
            .filter { it.type == type }
            .map { it.value to it }
            .toMap()
    }

    enum class Type {
        VALUE,
        KEYWORD,
        OPERATOR,
        OTHER
    }
}