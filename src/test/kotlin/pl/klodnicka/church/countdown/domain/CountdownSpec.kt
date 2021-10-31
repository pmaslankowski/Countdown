package pl.klodnicka.church.countdown.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.Instant

internal class CountdownSpec {

    private val clock: FixedClock = FixedClock()
    private val now: Instant = Instant.parse("2021-10-30T09:00:00.000Z")

    companion object {

        @JvmStatic
        fun elapsedTimeAndExpectedTimeLeftForOneMinuteCountdown() = listOf(
            Arguments.of(0.sec, TimeLeft.of(1.min)),
            Arguments.of(1.sec, TimeLeft.of(59.sec)),
            Arguments.of(30.sec, TimeLeft.of(30.sec)),
            Arguments.of(59.sec, TimeLeft.of(1.sec)),
            Arguments.of(60.sec, TimeLeft.of(0.sec)),
            Arguments.of(61.sec, TimeLeft.of(0.sec)),
            Arguments.of(2.min, TimeLeft.of(0.sec))
        )

        @JvmStatic
        fun elapsedTimeLessThanMinute() = listOf(0.sec, 1.sec, 30.sec, 59.sec)
            .map { Arguments.of(it) }

        @JvmStatic
        fun elapsedTimeLongerOrEqualToMinute() = listOf(1.min, 1.min and 1.sec, 2.min)
            .map { Arguments.of(it) }

        @JvmStatic
        fun elapsedTimeAndExpectedProgressForOneMinuteCountdown() = listOf(
            Arguments.of(0.sec, Progress.of(0)),
            Arguments.of(60.sec, Progress.of(100)),
            Arguments.of(30.sec, Progress.of(50)),
            Arguments.of(10.sec, Progress.of(16))
        )
    }

    @BeforeEach
    fun setup() {
        clock.fixAt(now)
    }

    @ParameterizedTest(name = "returns correct time left when {0} elapsed for 1 min countdown")
    @MethodSource("elapsedTimeAndExpectedTimeLeftForOneMinuteCountdown")
    fun `returns correct time left`(elapsedTime: Duration, expectedTimeLeft: TimeLeft) {
        // given
        val countdown = Countdown.started(clock, duration = 1.min)

        // and
        clock.advance(elapsedTime)

        // expect
        assertThat(countdown.timeLeft(clock)).isEqualTo(expectedTimeLeft)
    }

    @ParameterizedTest(name = "returns not finished after {0} elapsed for 1 min countdown")
    @MethodSource("elapsedTimeLessThanMinute")
    fun `returns not finished when countdown is still running`(elapsedTime: Duration) {
        // given
        val countdown = Countdown.started(clock, duration = 1.min)

        // and
        clock.advance(elapsedTime)

        // expect
        assertThat(countdown.finished(clock)).isFalse
    }

    @ParameterizedTest(name = "returns finished after {0} elapsed for 1 min countdown")
    @MethodSource("elapsedTimeLongerOrEqualToMinute")
    fun `returns finished when countdown has finished`(elapsedTime: Duration) {
        // given
        val countdown = Countdown.started(clock, duration = 1.min)

        // and
        clock.advance(elapsedTime)

        // expect
        assertThat(countdown.finished(clock)).isTrue
    }

    @ParameterizedTest(name = "returns progress {0} after {1} elapsed for 1 min countdown")
    @MethodSource("elapsedTimeAndExpectedProgressForOneMinuteCountdown")
    fun `returns correct progress`(elapsedTime: Duration, expectedProgress: Progress) {
        // given
        val countdown = Countdown.started(clock, duration = 1.min)

        // and
        clock.advance(elapsedTime)

        // expect
        assertThat(countdown.progress(clock)).isEqualTo(expectedProgress)
    }
}
