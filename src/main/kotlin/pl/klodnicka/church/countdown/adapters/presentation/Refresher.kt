package pl.klodnicka.church.countdown.adapters.presentation

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun refresher(fps: Long, refresh: () -> Unit): Job {
    val refreshInterval = 1000 / fps
    return ticker(refreshInterval)
        .onEach { refresh() }
        .launchIn(MainScope())
}

private fun ticker(periodMills: Long) = flow {
    while (true) {
        emit(Unit)
        delay(periodMills)
    }
}
