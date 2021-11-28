package pl.klodnicka.church.countdown.adapters.presentation.countdown

import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.domain.Countdown

@Component
class CountdownHolder {

    private var countdown: Countdown? = null

    fun get(): Countdown =
        countdown ?: throw IllegalStateException("No countdown has been stored")

    fun store(countdown: Countdown) {
        this.countdown = countdown
    }

    fun reset() {
        countdown = null
    }
}