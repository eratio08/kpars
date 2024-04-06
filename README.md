# Parser Combinator in Kotlin

A very simple [parser combinator](https://www.cs.nott.ac.uk/~pszgmh/pearl.pdf) library implementation in Kotlin.

## Example

The following grammar
```
expr ::= expr addop term | term
term ::= term mulop factor | factor
factor ::= digit | ( expr )
digit ::= 0 | 1 | . . . | 9
addop ::= + | -
mulop ::= * | /
```

Can be parsed and evaluated like this

```kotlin
// Parsers
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
// Evaluation
val res = apply(expr())("1 + 3 * 3").firstOrNull()?.first
// -> 10
```