package art.soft.protolang.exceptions

import art.soft.protolang.config.CompilerOptions
import art.soft.protolang.parser.Lexer
import java.lang.RuntimeException

class LexerException(message: String, val errors: List<Lexer.Error>): RuntimeException(message) {

    fun getDetailedMessage(options: CompilerOptions = CompilerOptions.DEFAULT): String =
        errors.joinToString("\n\n") { e ->
            e.position.getErrorMessage("LexerError: ${e.message}", options)
        }
}