package pl.klodnicka.church.countdown.adapters.presentation

import pl.klodnicka.church.countdown.adapters.presentation.countdownsetup.CountdownSetupViewModel
import pl.klodnicka.church.countdown.adapters.presentation.countdownsetup.CountdownSetupViewModelFactory
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.textfield

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
