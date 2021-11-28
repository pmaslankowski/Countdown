package pl.klodnicka.church.countdown.adapters.presentation

import pl.klodnicka.church.countdown.adapters.presentation.countdownsetup.CountdownSetupViewModel
import pl.klodnicka.church.countdown.adapters.presentation.countdownsetup.CountdownSetupViewModelFactory
import tornadofx.ValidationContext
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.textfield

class CountdownSetupView : View("Parametry odliczania") {

    private val viewModelFactory: CountdownSetupViewModelFactory by di()
    private val viewModel: CountdownSetupViewModel = viewModelFactory.createViewModel()

    override val root = form {
        fieldset("Parametry odliczania") {
            field("Długość") {
                textfield(viewModel.durationProperty) {
                    val ctx = ValidationContext()
                    ctx.addValidator(this, viewModel.durationProperty) {
                        if (!viewModel.validateDuration()) error("Wprowadź długość w formacie mm:ss") else null
                    }
                }
            }
            field("Ekran") {
                combobox(viewModel.selectedDisplayProperty, values = viewModel.availableDisplaysProperty)
            }
            buttonbar {
                button("Stop") {
                    action {
                        viewModel.stopCountdown()
                    }
                    enableWhen { viewModel.stopButtonEnabledProperty }
                }
                button("Start") {
                    action {
                        viewModel.startCountdown()
                    }
                    enableWhen { viewModel.startButtonEnabledProperty }
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
