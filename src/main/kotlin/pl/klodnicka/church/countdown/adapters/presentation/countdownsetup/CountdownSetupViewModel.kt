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
import pl.klodnicka.church.countdown.domain.DisplayProvider
import tornadofx.find
import tornadofx.getValue
import tornadofx.setValue
import java.time.Duration

class CountdownSetupViewModel(
    private val displayProvider: DisplayProvider,
    private val durationParser: DurationParser,
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

    val stopButtonDisabledProperty = SimpleBooleanProperty(true)
    var stopButtonDisabled by stopButtonDisabledProperty

    val startButtonDisabledProperty = SimpleBooleanProperty(false)
    var startButtonDisabled by startButtonDisabledProperty

    init {
        eventBus.subscribe<CountdownStartedEvent> { onCountdownStarted() }
        // this delay protects from a native deadlock between the prism renderer and the application thread on macos
        eventBus.subscribe<CountdownFinishedEvent>(delayMs = 600) { onCountdownFinished() }
    }

    fun refreshDisplays() {
        availableDisplays = FXCollections.observableArrayList(displayProvider.getAvailableDisplays())
        selectedDisplay = displayProvider.getPrimaryDisplay()
    }

    fun startCountdown() {
        val duration: Duration = durationParser.parse(duration)
        runCountdown(duration)
    }

    private fun runCountdown(duration: Duration) {
        val countdownScope = CountdownScope(duration, selectedDisplay)
        find<CountdownView>(countdownScope).openWindow(owner = null)
    }

    fun countdownSetupClosed() {
        eventBus.publish(ApplicationClosedEvent)
    }

    fun stopCountdown() {
        eventBus.publish(StopCountdownCommand)
    }

    private fun onCountdownStarted() {
        startButtonDisabled = true
        stopButtonDisabled = false
    }

    private fun onCountdownFinished() {
        startButtonDisabled = false
        stopButtonDisabled = true
    }
}

@Component
class CountdownSetupViewModelFactory(
    private val displayProvider: DisplayProvider,
    private val durationParser: DurationParser,
    private val eventBus: EventBus
) {

    fun createViewModel(): CountdownSetupViewModel =
        CountdownSetupViewModel(displayProvider, durationParser, eventBus)
}

object ApplicationClosedEvent : Event()
