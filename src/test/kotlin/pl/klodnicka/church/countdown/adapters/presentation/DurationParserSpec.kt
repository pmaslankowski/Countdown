package pl.klodnicka.church.countdown.adapters.presentation

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import pl.klodnicka.church.countdown.domain.and
import pl.klodnicka.church.countdown.domain.min
import pl.klodnicka.church.countdown.domain.sec
import java.time.Duration

internal class DurationParserSpec {

    private val parser: DurationParser = DurationParser()

    companion object {

        @JvmStatic
        fun durationsWithStringRepresentations() = listOf(
            Arguments.of("05:00", 5.min),
            Arguments.of("04:59", 4.min and 59.sec),
            Arguments.of("00:10", 10.sec),
            Arguments.of("00:04", 4.sec),
            Arguments.of("00:01", 1.sec),
            Arguments.of("00:00", 0.sec)
        )

        @JvmStatic
        fun invalidDurations() = listOf(
            "invalid duration",
            "0",
            "10",
            "5:00",
            "5:61",
            "99:99",
            "60:60",
            "05:50:51",
            "05:5",
            "12312",
            ":01",
            "01:"
        )

        @JvmStatic
        fun validDurations() = listOf("05:00", "04:59", "00:10", "00:04", "00:00")
    }

    @ParameterizedTest
    @MethodSource("durationsWithStringRepresentations")
    fun `parses valid durations`(durationAsString: String, expectedDuration: Duration) {
        // expect
        assertThat(parser.parse(durationAsString)).isEqualTo(expectedDuration)
    }

    @ParameterizedTest
    @MethodSource("invalidDurations")
    fun `throws exception when parsing invalid duration`(invalidDuration: String) {
        // expect
        assertThatThrownBy { parser.parse(invalidDuration) }.isInstanceOf(InvalidDurationException::class.java)
    }

    @ParameterizedTest
    @MethodSource("validDurations")
    fun `isValid returns true for valid durations`(validDuration: String) {
        // expect
        assertThat(parser.isValid(validDuration)).isTrue
    }

    @ParameterizedTest
    @MethodSource("invalidDurations")
    fun `isValid returns false for invalid durations`(invalidDuration: String) {
        assertThat(parser.isValid(invalidDuration)).isFalse
    }
}
