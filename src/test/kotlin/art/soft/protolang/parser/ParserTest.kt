package art.soft.protolang.parser

import org.junit.Test

class ParserTest {

    @Test fun testExpression() {
        val source = "1 + 2 + 3 * 4 == 13 + 2"

        val result = Parser(Lexer(source = source).tokenize()).expression()

        println(result)
    }
}