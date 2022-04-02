package art.soft.protolang.parser

data class SourcePosition(val row: Int, val col: Int, val indent: Int) {

    override fun toString() = "[$row:$col]"

    fun getErrorMessage(message: String, lines: Array<String>) = buildString {
        append("at line ").append(toString()).append('\n')
        try {
            append(lines[row - 1]).append('\n')
            append(" ".repeat(col)).append("^\n")
        } catch (ignored: ArrayIndexOutOfBoundsException) {}
        append(message)
    }
}
