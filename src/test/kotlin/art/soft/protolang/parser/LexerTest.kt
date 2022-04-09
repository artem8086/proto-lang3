package art.soft.protolang.parser

import art.soft.protolang.exceptions.LexerException
import art.soft.protolang.parser.TokenType.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LexerTest {

    @Test fun testNumber() {
        val input = "1_000 3.1415 0xCAFEBABE 0xf7_d6_c5 1e10 1.2e-2  12..10"

        val expList = listOf(NUMBER_INTEGER, NUMBER_FLOAT, NUMBER_INTEGER_HEX, NUMBER_INTEGER_HEX, NUMBER_FLOAT,
            NUMBER_FLOAT, NUMBER_INTEGER, RANGE, NUMBER_INTEGER)

        val result = Lexer(source = input).tokenize()

        assertEquals(expList, result.map { it.type })

        assertEquals(
            listOf("1000", "3.1415", "CAFEBABE", "f7d6c5", "1e+10", "1.2e-2", "12", "10"),
            result.map { it.value }.filter { it.isNotEmpty() }
        )
    }

    @Test fun testOctBinNumbers() {
        val input = "0o123 0o34_345 0b101 0b1111_1111"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(NUMBER_INTEGER_OCT, NUMBER_INTEGER_OCT, NUMBER_INTEGER_BIN, NUMBER_INTEGER_BIN),
            result.map { it.type }
        )

        assertEquals(
            listOf("123", "34345", "101", "11111111"),
            result.map { it.value }
        )
    }

    @Test fun testNumbersError() {
        val input = "3._14_ 0xf7_p6_s5 "
        assertFailsWith<LexerException> { Lexer(source = input).tokenize() }
    }

    @Test fun testArithmetic() {
        val input = "x=-1+((2*3)%(4/5))"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(IDENTIFIER, ASSIGN, MINUS, NUMBER_INTEGER, PLUS, LPAREN, LPAREN, NUMBER_INTEGER, MUL,
                NUMBER_INTEGER, RPAREN, MOD, LPAREN, NUMBER_INTEGER, DIV, NUMBER_INTEGER, RPAREN, RPAREN),
            result.map { it.type }
        )
        assertEquals("x", result[0].value)
    }

    @Test fun testKeywords() {
        val input = "if elif else for try catch finally break continue return not and or in is as let var type"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(IF, ELIF, ELSE, FOR, TRY, CATCH, FINALLY, BREAK, CONTINUE, RETURN, NOT, AND, OR, IN, IS, AS, LET, VAR, TYPE),
            result.map { it.type }
        )
    }

    @Test fun testIdentifiers() {
        val input = "\"text\\n\\ntext\" true false none this"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(STRING, BOOL_TRUE, BOOL_FALSE, NONE, THIS),
            result.map { it.type }
        )
        assertEquals("text\n\ntext", result[0].value)
    }

    @Test fun testIndentation() {
        val input = """
            main:
                if a == b:
                    var c = 0
                    while c < 5:
                        c += 1
                
                print("Hello world")
            """.trimIndent()

        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 1, 1, 1, 1),
            result.map { it.position.indent }
        )
    }

    @Test fun testMultilineString() {
        val input = """
            '''
            This is multiline string
            with json example:
            
            ''Double quotes''
            
            {
                "test": 123,
                "other": false
            }
            '''
        """

        val result = Lexer(source = input).tokenize()

        assertEquals(
            """
            This is multiline string
            with json example:
            
            ''Double quotes''
            
            {
                "test": 123,
                "other": false
            }
            """.trimIndent().trim(),
            result[0].value
        )
    }

    @Test fun testStringInterpolation() {
        val input = """$'Hello world: {$"Hello {1 + 2}" + $'b = [{3 + 3}]'}'"""

        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(STRING,
                INTERPOLATION_START, STRING,
                INTERPOLATION_START, NUMBER_INTEGER, PLUS, NUMBER_INTEGER, INTERPOLATION_END,
                STRING, PLUS, STRING,
                INTERPOLATION_START, NUMBER_INTEGER, PLUS, NUMBER_INTEGER, INTERPOLATION_END, STRING,
                INTERPOLATION_END, STRING),
            result.map { it.type }
        )
    }

    @Test fun testEmptyString() {
        val input = "\"\""
        val result = Lexer(source = input).tokenize()

        assertEquals(STRING, result[0].type)
        assertTrue(result[0].value.isEmpty())
    }

    @Test fun testOperators() {
        val input = "=+-*/%<>&|"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(ASSIGN, PLUS, MINUS, MUL, DIV, MOD, LT, GT, REFERENCE, PIPELINE),
            result.map { it.type }
        )
    }

    @Test fun testOperators2Char() {
        val input = "== != <= >= ==+ >=- -> >> << ??"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(EQ, NE, LTE, GTE, EQ, PLUS, GTE, MINUS, MINUS, GT, SHIFT_R, SHIFT_L, NULL_COALESCE),
            result.map { it.type }
        )
    }

    @Test fun testComments() {
        val input = """
            # comment
            1234
        """.trimIndent()
        val result = Lexer(source = input, collectComments = true).tokenize()

        assertEquals(
            listOf(COMMENT, NUMBER_INTEGER),
            result.map { it.type }
        )
    }

    @Test fun testSingleQuoteStingIdentifier() {
        val input = "`€` = 1"
        val result = Lexer(source = input).tokenize()

        assertEquals(
            listOf(IDENTIFIER, ASSIGN, NUMBER_INTEGER),
            result.map { it.type }
        )
    }
}