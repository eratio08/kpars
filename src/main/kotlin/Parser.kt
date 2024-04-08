package de.elurz

/**
 * A [Parser] is a function from String to a Pair the matches Char and the remaining unmatched String.
 * To allow the parser to fail a list is returned.
 * A parser that returns an empty list is considered a failed parser.
 */
typealias Parser<A> = (String) -> List<Pair<A, String>>

/**
 * Parser that parses a single character.
 */
fun item(): Parser<Char> = {
    when (it) {
        "" -> emptyList()
        else -> listOf(it.first() to it.drop(1))
    }
}

/**
 * Creates a Parser that reruns the given value and does not consume any input.
 */
fun <A> return_(a: A): Parser<A> = {
    listOf(a to it)
}

/**
 * Maps `Parser<A>` to `Parser<B>` using `f`.
 */
infix fun <A, B> Parser<A>.map(f: (A) -> B): Parser<B> = {
    this(it).map { (a, rest) -> f(a) to rest }
}

/**
 * Applies the result of `Parser<A>` to `f` to create `Parser<B>`.
 */
infix fun <A, B> Parser<A>.flatMap(f: (A) -> Parser<B>): Parser<B> = {
    this(it).flatMap { (a, rest) -> f(a)(rest) }
}

/**
 * Unit Parser.
 */
fun <A> zero(): Parser<A> = { emptyList() }

/**
 * Indeterministic choice operator, returns any matching Parser.
 */
infix fun <A> Parser<A>.both(p2: Parser<A>): Parser<A> = {
    this(it) + p2(it)
}

/**
 * Deterministic choice operator, return the first matching parser.
 */
infix fun <A> Parser<A>.or(p2: Parser<A>): Parser<A> = {
    this.both(p2)(it).firstOrNull()
        ?.let { listOf(it) }
        ?: emptyList()
}

/**
 * Conditional Parser, matches if the predicate is satisfied.
 */
fun satisfies(p: (Char) -> Boolean): Parser<Char> =
    item().flatMap<Char, Char> { if (p(it)) return_<Char>(it) else zero<Char>() }

/**
 * Parser matching the given character.
 */
fun char_(c: Char): Parser<Char> = satisfies { it == c }

/**
 * Parser matching the given string.
 */
fun string_(s: String): Parser<String> =
    if (s.isEmpty()) {
        return_<String>("")
    } else {
        val c = s.first()
        val rest = s.drop(1)
        char_(c) flatMap { string_(rest) } flatMap { return_("$c$rest") }
    }

/**
 * Applicative version of [map].
 */
fun <A, B> mapAp(fp: Parser<(A) -> B>, ap: Parser<A>): Parser<B> =
    fp flatMap { f -> ap.map { a -> f(a) } }

/** Fix point recursion. */
fun <A, B> fix(f: ((A) -> B) -> ((A) -> B)): (A) -> B = { a -> f(fix(f))(a) }

/**
 * List prepend constructor.
 */
fun <A> cons(a: A, l: List<A>): List<A> = buildList<A> { add(a);addAll(l) }
fun <A> cons(a: A): (List<A>) -> List<A> = { l -> cons(a, l) }

/**
 * Matches many of given parser `p`.
 */
fun <A> many(p: Parser<A>): Parser<List<A>> =
    fix { m -> mapAp(p.map(::cons), m) or return_(emptyList()) }

/**
 * Lifts a two ary function.
 */
fun <A, B, C> lift2(f: (A, B) -> C, pa: Parser<A>, pb: Parser<B>): Parser<C> =
    pa flatMap { a -> pb.map { b -> f(a, b) } }

/**
 * Like [many] but requires at least 1 match.
 * Trick here is that a single matching parser must match before the many parser.
 */
fun <A> many1(p: Parser<A>): Parser<List<A>> =
    lift2(::cons, p, many(p))

/**
 * Keeps the left parser and discards the right.
 */
infix fun <A, B> Parser<A>.keepLeft(p2: Parser<B>): Parser<A> = this flatMap { a -> p2 flatMap { return_(a) } }

/**
 * Discards the left parse and keep the right.
 */
infix fun <A, B> Parser<A>.keepRight(p2: Parser<B>): Parser<B> = this flatMap { p2 }

/**
 * parse parser `p` separated by parser `s`. needs to match at least once.
 */
fun <S, A> sepBy1(s: Parser<S>, p: Parser<A>): Parser<List<A>> =
    p flatMap { a -> many(s flatMap { p }) flatMap { l -> return_(buildList { add(a); addAll(l) }) } }

/**
 * Like [sepBy1] but matches 0 or more.
 */
fun <S, A> sepBy(s: Parser<S>, p: Parser<A>): Parser<List<A>> =
    sepBy1(s, p) or return_(emptyList())

/**
 * Alternately applies parser `p` and `op`. The result of `of` is assumed to be a left associative operator.
 * Matches 1 to n.
 */
fun <A> chainL1(p: Parser<A>, op: Parser<(A, A) -> A>): Parser<A> {
    fun rest(a: A): Parser<A> = op flatMap { f -> p flatMap { b -> rest(f(a, b)) } } or return_(a)

    return p flatMap ::rest
}

/**
 * Alternately applies parser `p` and `op`. The result of `of` is assumed to be a left associative operator.
 * Matches 0 to n.
 */
fun <A> chainL(p: Parser<A>, op: Parser<(A, A) -> A>, a: A): Parser<A> = chainL1(p, op) or return_(a)

/**
 * Alternately applies parser `p` and `op`. The result of `of` is assumed to be a right associative operator.
 * Matches 1 to n.
 */
fun <A> chainR1(p: Parser<A>, op: Parser<(A, A) -> A>): Parser<A> {
    fun rest(a: A): Parser<A> = op flatMap { f -> chainR1(p, op) flatMap { b -> rest(f(a, b)) } } or return_(a)

    return p flatMap ::rest
}

/**
 * Alternately applies parser `p` and `op`. The result of `of` is assumed to be a right associative operator.
 * Matches 0 to n.
 */
fun <A> chainR(p: Parser<A>, op: Parser<(A, A) -> A>, a: A): Parser<A> = chainR1(p, op) or return_(a)

/**
 * Parses the input as long `p` is satisfied. Matches 1 or more.
 */
fun takeWhile1(p: (Char) -> Boolean): Parser<String> =
    many1(satisfies(p)) map { chars -> chars.joinToString(separator = "") }

/**
 * Parses the input as long `p` is satisfied. Matches 0 or more.
 */
fun takeWhile(p: (Char) -> Boolean): Parser<String> =
    many(satisfies(p)) map { chars -> chars.joinToString(separator = "") }

fun isWhitespace(c: Char): Boolean = when (c) {
    ' ', '\n', '\r', '\n', '\t' -> true
    else -> false
}

/**
 * Matches whitespaces.
 */
fun whitespace(): Parser<String> =
    many(satisfies(::isWhitespace)) flatMap { cs -> return_(cs.joinToString(separator = "")) }

/**
 * Applies `p` and consumes trailing whitespace.
 */
fun <A> token(p: Parser<A>): Parser<A> = p flatMap { a -> whitespace() keepRight return_(a) }

fun symbol(s: String): Parser<String> = token(string_(s))

/**
 * Applies `p` and consumes leading spaces.
 */
fun <A> apply(p: Parser<A>): Parser<A> = whitespace() flatMap { p }