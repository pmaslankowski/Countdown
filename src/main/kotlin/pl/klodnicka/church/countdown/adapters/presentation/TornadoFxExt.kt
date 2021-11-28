package pl.klodnicka.church.countdown.adapters.presentation

import javafx.beans.value.ObservableValue
import javafx.scene.control.TextField
import tornadofx.ValidationContext
import tornadofx.ValidationMessage

inline fun <reified T> TextField.validate(
    property: ObservableValue<T>,
    noinline validator: ValidationContext.() -> ValidationMessage?
) {
    val ctx = ValidationContext()
    ctx.addValidator(this, property) { ctx.validator() }
}
