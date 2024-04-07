import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

typealias ParserResult<A> = List<Pair<A, String>>

class IsParserResult<A>(private val expected: List<Pair<A, String?>>) :
    TypeSafeMatcher<ParserResult<A>>() {
    override fun matchesSafely(item: ParserResult<A>?): Boolean =
        item?.let {
            if (item.size != expected.size) {
                false
            } else {
                expected.zip(item).fold(true) { acc, (expected, item) ->
                    if (expected.second == null) {
                        expected.first == item.first
                    } else {
                        expected.first == item.first && expected.second == item.second
                    }
                }
            }
        } == true

    override fun describeTo(description: Description) {
        expected.forEach { (expected, rest) ->
            if (rest != null) {
                description.appendValue(expected)
                    .appendText(" (")
                    .appendValue(expected!!::class.qualifiedName)
                    .appendText(") with remaining ")
                    .appendValue(rest)
            } else {
                description.appendValue(expected)
                    .appendText(" (")
                    .appendValue(expected!!::class.qualifiedName)
                    .appendText(")")
            }
        }
    }

    override fun describeMismatchSafely(item: ParserResult<A>?, description: Description) {
        if (item == null) {
            super.describeMismatchSafely(null, description)
        } else if (item.isEmpty()) {
            description.appendText("parsed nothing as the parser did not match")
        } else {
            description.appendText("Got ")
            item.forEach { (a, rest) ->
                description.appendValue(a)
                    .appendText(" (")
                    .appendValue(a!!::class.qualifiedName)
                    .appendText(") with remaining ")
                    .appendValue(rest)
                    .appendText(" ")
            }
            description.appendText(".")
        }
    }
}

fun <A> isParsedAs(expected: A, rest: String? = null): Matcher<ParserResult<A>> =
    IsParserResult(listOf(expected to rest))

fun <A> areParsedAs(expected: Pair<A, String?>, vararg expectedResults: Pair<A, String?>): Matcher<ParserResult<A>> =
    CoreMatchers.allOf(IsParserResult(buildList {
        add(expected.first to expected.second)
        addAll(expectedResults)
    }))

class IsNotMatched : TypeSafeMatcher<ParserResult<*>>() {
    override fun matchesSafely(item: ParserResult<*>?): Boolean =
        if (item == null) {
            false
        } else {
            item.isEmpty()
        }

    override fun describeTo(description: Description) {
        description.appendText("to not match the parser")
    }

    override fun describeMismatchSafely(item: ParserResult<*>?, description: Description) {
        if (item == null) {
            super.describeMismatchSafely(item, description)
        } else {
            description.appendText("got ")
            item.forEach { (a, rest) ->
                description.appendValue(a)
                    .appendText(" (")
                    .appendValue(a!!::class.qualifiedName)
                    .appendText(") with remaining ")
                    .appendValue(rest)
            }
            description.appendText(".")
        }
    }
}

fun isNotParsed(): Matcher<ParserResult<*>> = IsNotMatched()