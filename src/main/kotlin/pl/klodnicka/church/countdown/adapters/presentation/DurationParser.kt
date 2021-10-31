package pl.klodnicka.church.countdown.adapters.presentation

import org.springframework.stereotype.Component
import java.time.Duration

@Component
class DurationParser {

    fun parse(duration: String): Duration {
        if (!isValid(duration)) {
            throw InvalidDurationException(duration)
        }

        val (min, sec) = duration.split(MINUTES_SECONDS_SEPARATOR).map { it.toLong() }
        return Duration.ofMinutes(min) + Duration.ofSeconds(sec)
    }

    fun isValid(duration: String): Boolean {
        if (!duration.matches(VALID_DURATION_REGEX)) {
            return false
        }

        val (min, sec) = duration.split(MINUTES_SECONDS_SEPARATOR).map { it.toLong() }
        if (min > 59 || sec > 59) {
            return false
        }

        return true
    }

    companion object {
        const val MINUTES_SECONDS_SEPARATOR: String = ":"
        val VALID_DURATION_REGEX: Regex = """\d{2}:\d{2}""".toRegex()
    }
}

class InvalidDurationException(duration: String) : RuntimeException(
    "String $duration doesn't represent a valid duration"
)
