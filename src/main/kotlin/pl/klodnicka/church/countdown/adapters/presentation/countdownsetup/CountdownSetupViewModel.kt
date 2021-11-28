package pl.klodnicka.church.countdown.adapters.presentation.countdownsetup

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.adapters.Event
import pl.klodnicka.church.countdown.adapters.EventBus
import pl.klodnicka.church.countdown.adapters.presentation.CountdownScope
import pl.klodnicka.church.countdown.adapters.presentation.CountdownView
import pl.klodnicka.church.countdown.adapters.presentation.DurationParser
import pl.klodnicka.church.countdown.adapters.presentation.countdown.CountdownFinishedEvent
import pl.klodnicka.church.countdown.adapters.presentation.countdown.CountdownStartedEvent
import pl.klodnicka.church.countdown.adapters.presentation.countdown.StopCountdownCommand
import pl.klodnicka.church.countdown.domain.Display
import pl.klodnicka.church.countdown.domain.DisplayProvider
import tornadofx.find
import tornadofx.getValue
import tornadofx.setValue
import java.time.Duration

class CountdownSetupViewModel(
    private val displayProvider: DisplayProvider,
    private val durationParser: DurationParser,
    private val countdownRunner: CountdownRunner,
    private val eventBus: EventBus
) {

    val durationProperty = SimpleStringProperty("05:00")
    var duration by durationProperty

    val availableDisplaysProperty = SimpleListProperty(
        FXCollections.observableArrayList(
            displayProvider.getAvailableDisplays()
        )
    )
    var availableDisplays by availableDisplaysProperty

    val selectedDisplayProperty = SimpleObjectProperty(displayProvider.getPrimaryDisplay())
    var selectedDisplay by selectedDisplayProperty

    val stopButtonEnabledProperty = SimpleBooleanProperty(false)
    var stopButtonEnabled by stopButtonEnabledProperty

    val startButtonEnabledProperty = SimpleBooleanProperty(true)
    var startButtonEnabled by startButtonEnabledProperty

    private var isCountdownRunning: Boolean = false
    private var isDurationValid = true

    init {
        eventBus.subscribe(CountdownStartedEvent::class) { onCountdownStarted() }
        // this delay protects from a native deadlock between the prism renderer and the application thread on macos
        eventBus.subscribe(CountdownFinishedEvent::class, delayMs = 600) { onCountdownFinished() }
    }

    fun refreshDisplays() {
        availableDisplays = FXCollections.observableArrayList(displayProvider.getAvailableDisplays())
        selectedDisplay = displayProvider.getPrimaryDisplay()
    }

    fun startCountdown() {
        val duration: Duration = durationParser.parse(duration)
        countdownRunner.startCountdown(duration, selectedDisplay)
    }

    fun countdownSetupClosed() {
        eventBus.publish(ApplicationClosedEvent)
    }

    fun stopCountdown() {
        eventBus.publish(StopCountdownCommand)
    }

    fun validateDuration(): Boolean {
        isDurationValid = durationParser.isValid(duration)
        refreshButtonsEnabledState()
        return isDurationValid
    }

    fun onCountdownStarted() {
        isCountdownRunning = true
        refreshButtonsEnabledState()
    }

    fun onCountdownFinished() {
        isCountdownRunning = false
        refreshButtonsEnabledState()
    }

    private fun refreshButtonsEnabledState() {
        startButtonEnabled = isDurationValid && !isCountdownRunning
        stopButtonEnabled = isCountdownRunning
    }
}

@Component
class CountdownRunner {

    fun startCountdown(duration: Duration, selectedDisplay: Display) {
        val countdownScope = CountdownScope(duration, selectedDisplay)
        find<CountdownView>(countdownScope).openWindow(owner = null)
    }
}

@Component
class CountdownSetupViewModelFactory(
    private val displayProvider: DisplayProvider,
    private val durationParser: DurationParser,
    private val countdownRunner: CountdownRunner,
    private val eventBus: EventBus
) {

    fun createViewModel(): CountdownSetupViewModel =
        CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)
}

object ApplicationClosedEvent : Event()
