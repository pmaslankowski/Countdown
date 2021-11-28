package pl.klodnicka.church.countdown.adapters.presentation.countdown

import javafx.beans.property.SimpleStringProperty
import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.adapters.Command
import pl.klodnicka.church.countdown.adapters.Event
import pl.klodnicka.church.countdown.adapters.EventBus
import pl.klodnicka.church.countdown.adapters.presentation.countdownsetup.ApplicationClosedEvent
import pl.klodnicka.church.countdown.domain.Clock
import pl.klodnicka.church.countdown.domain.Countdown
import tornadofx.getValue
import tornadofx.setValue
import java.time.Duration

class CountdownViewModel(
    private val clock: Clock,
    private val countdownHolder: CountdownHolder,
    private val eventBus: EventBus
) {

    val timeLeftProperty = SimpleStringProperty("")
    private var timeLeft by timeLeftProperty

    private var closeWindowCallback: () -> Unit = {}

    init {
        eventBus.subscribe<StopCountdownCommand> { onStopCountdownCommand() }
        eventBus.subscribe<ApplicationClosedEvent> { onApplicationClosed() }
    }

    fun startCountdown(duration: Duration, closeWindowCallback: () -> Unit) {
        val countdown = Countdown.started(clock, duration)
        countdownHolder.store(countdown)
        this.closeWindowCallback = closeWindowCallback
        eventBus.publish(CountdownStartedEvent)
    }

    fun refresh() {
        val countdown: Countdown = countdownHolder.get()
        if (countdown.finished(clock)) {
            startFinishing()
        }
        timeLeft = countdown.timeLeft(clock).toString()
    }

    private fun startFinishing() {
        closeWindowCallback()
    }

    fun finalizeFinishing() {
        countdownHolder.reset()
        eventBus.publish(CountdownFinishedEvent)
    }

    private fun onStopCountdownCommand() {
        startFinishing()
    }

    private fun onApplicationClosed() {
        startFinishing()
    }
}

@Component
class CountdownViewModelFactory(
    private val clock: Clock,
    private val countdownHolder: CountdownHolder,
    private val eventBus: EventBus
) {

    fun createViewModel(): CountdownViewModel =
        CountdownViewModel(clock, countdownHolder, eventBus)
}

object StopCountdownCommand : Command()
object CountdownStartedEvent : Event()
object CountdownFinishedEvent : Event()