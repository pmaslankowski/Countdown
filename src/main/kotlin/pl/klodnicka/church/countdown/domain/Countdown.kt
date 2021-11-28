package pl.klodnicka.church.countdown.domain

import java.time.Duration
import java.time.Instant

class Countdown private constructor(
    private val startedAt: Instant,
    private val duration: Duration,
) {

    fun timeLeft(clock: Clock): TimeLeft {
        val elapsed = Duration.between(startedAt, clock.now())
        return TimeLeft.of(duration - elapsed)
    }

    fun finished(clock: Clock): Boolean =
        timeLeft(clock) == TimeLeft.ZERO

    fun progress(clock: Clock): Progress {
        val elapsedMillis: Long = Duration.between(startedAt, clock.now()).toMillis()
        val totalMillis = duration.toMillis()
        return Progress.of(100 * elapsedMillis / totalMillis)
    }

    companion object {

        fun started(clock: Clock, duration: Duration): Countdown = Countdown(
            startedAt = clock.now(),
            duration = duration
        )
    }
}

data class TimeLeft private constructor(
    val raw: Duration
) {

    override fun toString(): String =
        "${raw.toMinutesPart().toZeroPaddedString()}:${raw.toSecondsPart().toZeroPaddedString()}"

    private fun Int.toZeroPaddedString(): String = toString().padStart(2, '0')

    companion object {

        fun of(duration: Duration): TimeLeft = when {
            duration.isNegative || duration.isZero -> ZERO
            else -> TimeLeft(duration)
        }

        val ZERO = TimeLeft(Duration.ZERO)
    }
}

data class Progress private constructor(
    val value: Int
) {

    companion object {

        fun of(progress: Long) = when {
            progress < 0 -> Progress(0)
            progress > 100 -> Progress(100)
            else -> Progress(progress.toInt())
        }
    }
}
