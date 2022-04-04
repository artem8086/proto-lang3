package art.soft.protolang.parser

class LexerResult(val tokens: List<Token>, val lines: List<Line>, val source: String) {

    interface Line {
        val start: Int

        val end: Int

        val indent: Int
    }

    fun line(pos: SourcePosition): String {
        val line = lines[pos.row - 1]

        return source.substring(line.start, line.end)
    }
}
