package pl.klodnicka.church.countdown.domain

import java.time.Duration
import java.time.Instant

class FixedClock : Clock {

    @Volatile
    private var currentTime: Instant = Instant.now()

    override fun now(): Instant = currentTime

    fun fixAt(fixedTime: Instant) {
        currentTime = fixedTime
    }

    fun advance(duration: Duration) {
        currentTime += duration
    }
}

val Int.sec: Duration get() = Duration.ofSeconds(this.toLong())
val Int.min: Duration get() = Duration.ofMinutes(this.toLong())
infix fun Duration.and(other: Duration): Duration = this + other
