package art.soft.protolang.exceptions

import art.soft.protolang.parser.Lexer
import java.lang.RuntimeException

class LexerException(message: String, errors: List<Lexer.Error>): RuntimeException(message)