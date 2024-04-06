import de.elurz.Parser
import de.elurz.apply
import de.elurz.chainL1
import de.elurz.flatMap
import de.elurz.keepLeft
import de.elurz.keepRight
import de.elurz.or
import de.elurz.return_
import de.elurz.satisfies
import de.elurz.symbol
import de.elurz.token

/**
 * Implements the following grammar
 * ```
 *    expr ::= expr addop term | term
 *    term ::= term mulop factor | factor
 *    factor ::= digit | ( expr )
 *    digit ::= 0 | 1 | . . . | 9
 *    addop ::= + | -
 *    mulop ::= * | /
 * ```
 */
fun add(a: Int, b: Int): Int = a + b
fun sub(a: Int, b: Int): Int = a - b
val addOp: Parser<(Int, Int) -> Int> =
    (symbol("+") keepRight return_(::add)) or (symbol("-") keepRight return_(::sub))

fun mul(a: Int, b: Int): Int = a * b
fun div(a: Int, b: Int): Int = a / b
val mulOp: Parser<(Int, Int) -> Int> =
    (symbol("*") keepRight return_(::mul)) or (symbol("/") keepRight return_(::div))

fun isDigit(c: Char): Boolean = when (c) {
    in '0'..'9' -> true
    else -> false
}

val digit = token(satisfies(::isDigit)) flatMap { x -> return_(x.code - '0'.code) }

fun expr(): Parser<Int> {
    val factor = digit or (symbol("(") flatMap { _ -> expr() keepLeft symbol(")") })
    val term = chainL1(factor, mulOp)

    return chainL1(term, addOp)
}

fun test(str: String, expected: Int) {
    val res = apply(expr())(str).firstOrNull()?.first
    require(res == expected) { "Case `$str` : expected $expected but got $res" }
}

fun main() {
    listOf(
        "2 + 5" to 7,
        "4 - 5" to -1,
        "2 + 5 - 4" to 3,
        "5 * 2" to 10,
        "5 * 2 * 4" to 40,
        "9 / 3" to 3,
        "1 + 2 * 3 - 4" to 3,
        "(1 + 2) * (3 - 4)" to -3,
    ).forEach { (str, expected) -> test(str, expected) }
}