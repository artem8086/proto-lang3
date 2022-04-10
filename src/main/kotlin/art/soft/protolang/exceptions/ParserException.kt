package art.soft.protolang.exceptions

import art.soft.protolang.config.CompilerOptions
import art.soft.protolang.parser.Parser

class ParserException(message: String, val errors: List<Parser.Error>): RuntimeException(message) {

    fun getDetailedMessage(options: CompilerOptions = CompilerOptions.DEFAULT): String =
        errors.joinToString("\n\n") { e ->
            e.position.getErrorMessage("ParserError: ${e.exception.message}", options)
        }
}