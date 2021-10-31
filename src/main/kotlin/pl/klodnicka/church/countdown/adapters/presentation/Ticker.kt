package pl.klodnicka.church.countdown.adapters.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun ticker(periodMills: Long) = flow {
    while (true) {
        emit(Unit)
        delay(periodMills)
    }
}
