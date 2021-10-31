package pl.klodnicka.church.countdown.adapters.presentation

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.adapters.Event
import pl.klodnicka.church.countdown.adapters.EventBus
import pl.klodnicka.church.countdown.domain.DisplayProvider
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.find
import tornadofx.form
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.textfield
import java.time.Duration

// TODO: duration validation
class CountdownSetupView : View("Parametry odliczania") {

    private val viewModelFactory: CountdownSetupViewModelFactory by di()
    private val viewModel: CountdownSetupViewModel = viewModelFactory.createViewModel()

    override val root = form {
        fieldset("Parametry odliczania") {
            field("Długość") {
                textfield(viewModel.durationProperty)
            }
            field("Ekran") {
                combobox(viewModel.selectedDisplayProperty, values = viewModel.availableDisplaysProperty)
            }
            buttonbar {
                button("Stop") {
                    action {
                        viewModel.stopCountdown()
                    }
                    disableProperty().bind(viewModel.stopButtonDisabledProperty)
                }
                button("Start") {
                    action {
                        viewModel.startCountdown()
                    }
                    disableProperty().bind(viewModel.startButtonDisabledProperty)
                }
            }
        }
    }

    override fun onDock() {
        viewModel.refreshDisplays()
    }

    override fun onUndock() {
        viewModel.countdownSetupClosed()
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

object ApplicationClosedEvent : Event()
