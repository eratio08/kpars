import de.elurz.Parser
import de.elurz.both
import de.elurz.char_
import de.elurz.flatMap
import de.elurz.item
import de.elurz.keepLeft
import de.elurz.keepRight
import de.elurz.many
import de.elurz.many1
import de.elurz.map
import de.elurz.or
import de.elurz.return_
import de.elurz.satisfies
import de.elurz.sepBy
import de.elurz.sepBy1
import de.elurz.string_
import de.elurz.zero
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class ParserTest {

    @Nested
    inner class Item {
        @Test
        fun `should parse single character and keep the rest`() {
            //given
            val input = "abc"

            //when
            val result = item()(input)

            //then
            assertThat(result, isParsedAs('a', "bc"))
        }

        @Test
        fun `should parse single character and keep without rest`() {
            //given
            val input = "a"

            //when
            val result = item()(input)

            //then
            assertThat(result, isParsedAs('a', ""))
        }

        @Test
        fun `should fail on empty input`() {
            //given
            val input = ""

            //when
            val result = item()(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class Return {
        @Test
        fun `should return a parser parsing the given value and not consume any input`() {
            //given
            val input = "abc"
            val value = Int.MAX_VALUE

            //when
            val result = return_(value)(input)

            //then
            assertThat(result, isParsedAs(value, input))
        }
    }

    @Nested
    inner class Map {
        @Test
        fun `should map function over parser`() {
            //given
            val input = "abc"
            val f: (Char) -> String = { it + "d" }

            //when
            val result = item().map(f)(input)

            //then
            assertThat(result, isParsedAs("ad", "bc"))
        }

        @Test
        fun `should not map on a failed parser`() {
            //given
            val input = "abc"
            val f: (String) -> String = { it + "d" }

            //when
            val result = zero<String>().map(f)(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class FlatMap {
        @Test
        fun `should sequence operation to parser`() {
            //given
            val input = "abc"
            val f: (Char) -> Parser<Int> = { return_(it.code) }

            //when
            val result = item().flatMap(f)(input)

            //then
            assertThat(result, isParsedAs(97, "bc"))
        }

        @Test
        fun `should not sequence operation on failed parser`() {
            //given
            val input = "abc"
            val f: (Char) -> Parser<Int> = { return_(it.code) }

            //when
            val result = zero<Char>().flatMap(f)(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class Zero {
        @Test
        fun `should not parse`() {
            //given
            val input = "abc"

            //when
            val result = zero<Any>()(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class Both {
        @Test
        fun `should return first and second parser if both match`() {
            //given
            val input = "abc"

            //when
            val results = (string_("a") both string_("ab"))(input)

            //then
            assertThat(results, areParsedAs("a" to "bc", "ab" to "c"))
        }

        @Test
        fun `should return first parser if only it matches`() {
            //given
            val input = "abc"

            //when
            val results = (string_("a") both string_("b"))(input)

            //then
            assertThat(results, isParsedAs("a", "bc"))
        }

        @Test
        fun `should return second parser if only it matches`() {
            //given
            val input = "abc"

            //when
            val results = (string_("ac") both string_("ab"))(input)

            //then
            assertThat(results, isParsedAs("ab", "c"))
        }
    }

    @Nested
    inner class Or {
        @Test
        fun `should return first parser if both match`() {
            //given
            val input = "abc"

            //when
            val results = (string_("a") or string_("ab"))(input)

            //then
            assertThat(results, isParsedAs("a", "bc"))
        }

        @Test
        fun `should return first parser if only it matches`() {
            //given
            val input = "abc"

            //when
            val results = (string_("a") or string_("b"))(input)

            //then
            assertThat(results, isParsedAs("a", "bc"))
        }

        @Test
        fun `should return second parser if only it matches`() {
            //given
            val input = "abc"

            //when
            val results = (string_("b") or string_("ab"))(input)

            //then
            assertThat(results, isParsedAs("ab", "c"))
        }
    }

    @Nested
    inner class Satisfies {
        @Test
        fun `should parse on satisfied predicate`() {
            //given
            val input = "1bc"
            val predicate: (Char) -> Boolean = { it.toString().toInt() == 1 }

            //when
            val result = satisfies(predicate)(input)

            //then
            assertThat(result, isParsedAs('1'))
        }

        @Test
        fun `should not parse on unsatisfied predicate`() {
            //given
            val input = "abc"
            val predicate: (Char) -> Boolean = { false }

            //when
            val result = satisfies(predicate)(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class Char_ {
        @Test
        fun `should parse given char if parser matches`() {
            //given
            val input = "abc"

            //when
            val result = char_('a')(input)

            //then
            assertThat(result, isParsedAs('a'))
        }

        @Test
        fun `should not parse given char if parser does not match`() {
            //given
            val input = "abc"

            //when
            val result = char_('b')(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class String_ {
        @Test
        fun `should parse given string if parser matches`() {
            //given
            val input = "abc"

            //when
            val result = string_("ab")(input)

            //then
            assertThat(result, isParsedAs("ab", "c"))
        }

        @Test
        fun `should not parse given string if parser does not matches`() {
            //given
            val input = "abc"

            //when
            val result = string_("ac")(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class Many {
        @Test
        fun `should parse a single match of the given parser`() {
            //given
            val input = "abc"

            //when
            val result = many(char_('a'))(input)

            //then
            assertThat(result, isParsedAs(listOf('a'), "bc"))
        }

        @Test
        fun `should parse multiple matches of the given parser`() {
            //given
            val input = "abcabcd"

            //when
            val result = many(string_("abc"))(input)

            //then
            assertThat(result, isParsedAs(listOf("abc", "abc"), "d"))
        }

        @Test
        fun `should succeed parser if does not match`() {
            //given
            val input = "abcabcd"

            //when
            val result = many(string_("ac"))(input)

            //then
            assertThat(result, isParsedAs(listOf(), input))
        }
    }

    @Nested
    inner class Many1 {
        @Test
        fun `should parse a single match of the given parser`() {
            //given
            val input = "abc"

            //when
            val result = many1(char_('a'))(input)

            //then
            assertThat(result, isParsedAs(listOf('a'), "bc"))
        }

        @Test
        fun `should parse multiple matches of the given parser`() {
            //given
            val input = "abcabcd"

            //when
            val result = many1(string_("abc"))(input)

            //then
            assertThat(result, isParsedAs(listOf("abc", "abc"), "d"))
        }

        @Test
        fun `should fail if parser does not match at least once`() {
            //given
            val input = "abcabcd"

            //when
            val result = many1(string_("ac"))(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class KeepLeft {
        @Test
        fun `should keep the result of the left parser`() {
            //given
            val input = "abc"

            //when
            val result = (char_('a') keepLeft char_('b'))(input)

            //then
            assertThat(result, isParsedAs('a', "c"))
        }

        @Test
        fun `should not run the right parser if the left parser did not match`() {
            //given
            val input = "abc"

            //when
            val result = (char_('c') keepLeft char_('b'))(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class KeepRight {
        @Test
        fun `should keep the result of the right parser`() {
            //given
            val input = "abc"

            //when
            val result = (char_('a') keepRight char_('b'))(input)

            //then
            assertThat(result, isParsedAs('b', "c"))
        }

        @Test
        fun `should not run the right parser if the left parser did not match`() {
            //given
            val input = "abc"

            //when
            val result = (char_('c') keepRight char_('b'))(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class SepBy1 {
        @Test
        fun `should match parser separated by other parser given at least one match`() {
            //given
            val input = "ab,ab,ab"

            //when
            val result = sepBy1(char_(','), string_("ab"))(input)

            //then
            assertThat(result, isParsedAs(listOf("ab", "ab", "ab"), ""))
        }

        @Test
        fun `should should not consume separator if not in middle position`() {
            //given
            val input = "ab,"

            //when
            val result = sepBy1(char_(','), string_("ab"))(input)

            //then
            assertThat(result, isParsedAs(listOf("ab"), ","))
        }

        @Test
        fun `should parse if not separator is matched`() {
            //given
            val input = "ab"

            //when
            val result = sepBy1(char_(','), string_("ab"))(input)

            //then
            assertThat(result, isParsedAs(listOf("ab"), ""))
        }

        @Test
        fun `should fail if not match`() {
            //given
            val input = "abc,abc"

            //when
            val result = sepBy1(char_(','), string_("bc"))(input)

            //then
            assertThat(result, isNotParsed())
        }
    }

    @Nested
    inner class SepBy {
        @Test
        fun `should match parser separated by other parser given at least one match`() {
            //given
            val input = "ab,ab"

            //when
            val result = sepBy(char_(','), string_("ab"))(input)

            //then
            assertThat(result, isParsedAs(listOf("ab", "ab"), ""))
        }

        @Test
        fun `should not consume separator of not in separating position`() {
            //given
            val input = "ab,"

            //when
            val result = sepBy(char_(','), string_("ab"))(input)

            //then
            assertThat(result, isParsedAs(listOf("ab"), ","))
        }

        @Test
        fun `should match given no separator`() {
            //given
            val input = "ab"

            //when
            val result = sepBy(char_(','), string_("ab"))(input)

            //then
            assertThat(result, isParsedAs(listOf("ab"), ""))
        }

        @Test
        fun `should not fail on not matching`() {
            //given
            val input = "abc,abc"

            //when
            val result = sepBy(char_(','), string_("bc"))(input)

            //then
            assertThat(result, isParsedAs(emptyList(), "abc,abc"))
        }
    }
}