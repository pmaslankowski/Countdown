package pl.klodnicka.church.countdown.adapters

import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.domain.Clock
import java.time.Instant

@Component
class SystemClock : Clock {

    override fun now(): Instant = Instant.now()
}
