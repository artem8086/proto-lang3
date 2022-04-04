package art.soft.protolang.parser

import art.soft.protolang.config.CompilerOptions

interface SourcePosition {

    val filename: String?

    val source: String?

    val row: Int

    val col: Int

    val indent: Int

    val lineStart: Int

    val lineEnd: Int

    fun getErrorMessage(message: String, options: CompilerOptions): String
}
