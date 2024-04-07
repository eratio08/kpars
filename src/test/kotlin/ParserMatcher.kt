import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

typealias ParserResult<A> = List<Pair<A, String>>

class IsParserResult<A>(private val expected: A, private val rest: String? = null) :
    TypeSafeMatcher<ParserResult<A>>() {
    override fun matchesSafely(item: ParserResult<A>?): Boolean =
        item?.firstOrNull()?.let { (a, r) ->
            if (rest != null) a == expected && rest == r
            else a == expected
        } == true

    override fun describeTo(description: Description) {
        if (rest != null) {
            description.appendText("to parse ")
                .appendValue(expected)
                .appendText(" (")
                .appendValue(expected!!::class.qualifiedName)
                .appendText(") and remaining ")
                .appendValue(rest)
        } else {
            description.appendText("to parse ")
                .appendValue(expected)
                .appendText(" (")
                .appendValue(expected!!::class.qualifiedName)
                .appendText(")")
        }
    }

    override fun describeMismatchSafely(item: ParserResult<A>?, description: Description) {
        if (item == null) {
            super.describeMismatchSafely(null, description)
        } else if (item.isEmpty()) {
            description.appendText("parsed nothing as the parser did not match")
        } else {
            description.appendText("parsed ")
            item.forEach { (a, rest) ->
                description.appendValue(a)
                    .appendText(" (")
                    .appendValue(a!!::class.qualifiedName)
                    .appendText(") with remaining ")
                    .appendValue(rest)
            }
        }
    }
}

fun <A> isParsedAs(expected: A, rest: String? = null): Matcher<ParserResult<A>> =
    IsParserResult(expected, rest)

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
            description.appendText("parsed ")
            item.forEach { (a, rest) ->
                description.appendValue(a)
                    .appendText(" (")
                    .appendValue(a!!::class.qualifiedName)
                    .appendText(") with remaining ")
                    .appendValue(rest)
            }
        }
    }
}

fun isNotParsed(): Matcher<ParserResult<*>> = IsNotMatched()