package pl.klodnicka.church.countdown.adapters.presentation

import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import kotlinx.coroutines.Job
import pl.klodnicka.church.countdown.adapters.FxWindow
import pl.klodnicka.church.countdown.adapters.presentation.countdown.CountdownViewModel
import pl.klodnicka.church.countdown.adapters.presentation.countdown.CountdownViewModelFactory
import pl.klodnicka.church.countdown.domain.Display
import tornadofx.Fragment
import tornadofx.Scope
import tornadofx.hbox
import tornadofx.label
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

data class CountdownScope(val duration: Duration, val display: Display) : Scope()
