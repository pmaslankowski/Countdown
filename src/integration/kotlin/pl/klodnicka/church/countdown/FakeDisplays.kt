package pl.klodnicka.church.countdown

import pl.klodnicka.church.countdown.adapters.FxWindow
import pl.klodnicka.church.countdown.domain.Display
import pl.klodnicka.church.countdown.domain.DisplayProvider
import pl.klodnicka.church.countdown.domain.Window
import java.lang.IllegalStateException

class FakeDisplays(private val displayProvider: FakeDisplayProvider) {

    fun thereAreDisplays(primaryDisplay: FakeDisplay, vararg others: FakeDisplay) {
        displayProvider.stubPrimaryDisplay(primaryDisplay)
        displayProvider.stubOtherDisplays(*others)
    }
}

class FakeDisplay(
    override val name: String = "Fake display",
    override val width: Double = 800.0,
    override val height: Double = 600.0,
    override val minX: Double = 0.0,
    override val minY: Double = 0.0,
    private val windowsShown: MutableList<Window> = mutableListOf()
) : Display {

    override fun label(): String = name

    override fun toString(): String = label()

    override fun showFullScreenWindow(window: Window) {
        windowsShown.add(window)
        moveWindowSoThatItDoesNotCoverTheOriginalWindow(window)
    }

    private fun moveWindowSoThatItDoesNotCoverTheOriginalWindow(window: Window) {
        if (window !is FxWindow) {
            throw IllegalStateException("Fake display supports only FX windows")
        }
        window.stage.apply {
            x = 1000.0
            y = 1000.0
        }
    }

    fun isShowing(window: Window): Boolean =
        windowsShown.contains(window)
}

class FakeDisplayProvider : DisplayProvider {

    private var primaryDisplay: Display = FakeDisplay()
    private var otherDisplays: List<Display> = listOf()

    override fun getAvailableDisplays(): List<Display> = listOf(primaryDisplay) + otherDisplays

    override fun getPrimaryDisplay(): Display = primaryDisplay

    fun stubPrimaryDisplay(display: FakeDisplay) {
        primaryDisplay = display
    }

    fun stubOtherDisplays(vararg displays: FakeDisplay) {
        otherDisplays = displays.asList()
    }
}
