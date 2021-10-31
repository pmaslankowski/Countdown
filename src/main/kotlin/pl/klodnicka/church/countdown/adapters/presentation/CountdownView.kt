package pl.klodnicka.church.countdown.adapters.presentation

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.adapters.Command
import pl.klodnicka.church.countdown.adapters.Event
import pl.klodnicka.church.countdown.adapters.EventBus
import pl.klodnicka.church.countdown.adapters.FxWindow
import pl.klodnicka.church.countdown.domain.Clock
import pl.klodnicka.church.countdown.domain.Countdown
import pl.klodnicka.church.countdown.domain.Display
import tornadofx.Fragment
import tornadofx.Scope
import tornadofx.getValue
import tornadofx.hbox
import tornadofx.label
import tornadofx.setValue
import tornadofx.vbox
import java.time.Duration

class CountdownView : Fragment("Odliczanie") {

    override val scope = super.scope as CountdownScope
    private val viewModelFactory: CountdownViewModelFactory by di()
    private val viewModel: CountdownViewModel = viewModelFactory.createViewModel()
    private val refresher: Job

    init {
        viewModel.startCountdown(scope.duration, closeWindowCallback = { close() })
        refresher = refresher(fps = 60) {
            viewModel.refresh()
        }
    }

    override val root = hbox(alignment = CENTER) {
        vbox(alignment = CENTER) {
            label(viewModel.timeLeftProperty)
        }
        toBeShownOnAnotherScreen()
    }

    private fun HBox.toBeShownOnAnotherScreen() {
        setPrefSize(800.0, 600.0)
    }

    override fun onDock() {
        currentStage?.let {
            scope.display.showFullScreenWindow(FxWindow(it))
        }
    }

    override fun onUndock() {
        refresher.cancel()
        viewModel.finalizeFinishing()
    }
}

fun refresher(fps: Long, refresh: () -> Unit): Job {
    val refreshInterval = 1000 / fps
    return ticker(refreshInterval)
        .onEach { refresh() }
        .launchIn(MainScope())
}

data class CountdownScope(val duration: Duration, val display: Display) : Scope()

@Component
class CountdownViewModelFactory(
    private val clock: Clock,
    private val countdownHolder: CountdownHolder,
    private val eventBus: EventBus
) {

    fun createViewModel(): CountdownViewModel =
        CountdownViewModel(clock, countdownHolder, eventBus)
}

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

object StopCountdownCommand : Command()
object CountdownStartedEvent : Event()
object CountdownFinishedEvent : Event()
