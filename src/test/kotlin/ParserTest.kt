import de.elurz.Parser
import de.elurz.flatMap
import de.elurz.item
import de.elurz.map
import de.elurz.return_
import de.elurz.zero
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class ParserTest {

    @Nested
    inner class Item() {
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
    inner class Return() {
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
    inner class Map() {
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
    inner class FlatMap() {
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
}