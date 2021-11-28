package pl.klodnicka.church.countdown.domain

import java.time.Instant

interface Clock {

    fun now(): Instant
}
