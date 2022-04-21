package art.soft.protolang.parser

import org.junit.Test

class ParserTest {

    @Test fun testExpression() {
        val source = "1 + 3 * 4 == 13"

        val result = Parser(Lexer(source = source).tokenize()).expression()

        print(result)
    }
}