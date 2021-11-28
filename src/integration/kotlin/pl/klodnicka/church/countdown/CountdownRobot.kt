package pl.klodnicka.church.countdown

import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Labeled
import javafx.stage.Stage
import javafx.stage.Window
import org.testfx.api.FxRobot
import org.testfx.assertions.api.Assertions
import org.testfx.util.WaitForAsyncUtils
import pl.klodnicka.church.countdown.adapters.FxWindow
import java.lang.IllegalArgumentException
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class CountdownRobot(
    private val fxRobot: FxRobot
) {

    fun startCountdown() {
        fxRobot.interact { countdownSetupWindow().requestFocus() }
        assertStartButtonStateIs(disabled = false)
        fxRobot.clickOn(START_BUTTON_LABEL)
    }

    fun stopCountdown() {
        fxRobot.interact { countdownSetupWindow().requestFocus() }
        assertStopButtonStateIs(disabled = false)
        fxRobot.clickOn(STOP_BUTTON_LABEL)
    }

    fun selectDisplay(display: FakeDisplay) {
        val comboBox: ComboBox<FakeDisplay> = fxRobot.lookup<ComboBox<FakeDisplay>> { true }.queryComboBox()
        fxRobot.interact { comboBox.selectionModel.select(display) }
    }

    fun closeCountdownSetupWindow() {
        close(countdownSetupWindow())
    }

    fun assertCountdownIsShowing(timeLeft: String) {
        val label = countdownLabel()
        waitFor(messageOnFail = "Countdown is not showing") {
            label.text == timeLeft
        }
    }

    fun assertCountdownClosed(timeoutMs: Long = 10000) {
        waitFor(messageOnFail = "Countdown has not been closed", timeoutMs = timeoutMs) {
            fxRobot.listWindows().none { it is Stage && it.title?.equals(COUNTDOWN_WINDOW_TITLE) ?: false }
        }
    }

    fun assertStartButtonStateIs(disabled: Boolean) {
        waitFor(
            messageOnFail = "Start button is ${toState(!disabled)} but was expected to be ${toState(disabled)}",
            timeoutMs = 1000
        ) {
            fxRobot.lookup(START_BUTTON_LABEL).query<Button>().isDisabled == disabled
        }
    }

    fun assertStopButtonStateIs(disabled: Boolean) {
        waitFor(
            messageOnFail = "Stop button is ${toState(!disabled)} but was expected to be ${toState(disabled)}",
            timeoutMs = 1000
        ) {
            fxRobot.lookup(STOP_BUTTON_LABEL).query<Button>().isDisabled == disabled
        }
    }

    fun assertCountdownIsShownOn(display: FakeDisplay) {
        Assertions.assertThat(display.isShowing(countdownWindow().asFxWindow())).isTrue
    }

    private fun waitFor(messageOnFail: String, timeoutMs: Long = 100, condition: Callable<Boolean>) {
        try {
            WaitForAsyncUtils.waitFor(timeoutMs, TimeUnit.MILLISECONDS, condition)
        } catch (ex: TimeoutException) {
            throw AssertionError(messageOnFail)
        }
    }

    private fun countdownLabel(): Labeled = fxRobot.from(fxRobot.rootNode(countdownWindow())).lookup(".label").query()

    private fun countdownWindow(): Window = fxRobot.window(COUNTDOWN_WINDOW_TITLE)

    private fun countdownSetupWindow(): Window = fxRobot.window(COUNTDOWN_SETUP_WINDOW_TITLE)

    private fun close(window: Window) {
        fxRobot.interact { (window as Stage).close() }
    }

    private fun toState(disabled: Boolean) =
        if (disabled) {
            "disabled"
        } else {
            "enabled"
        }

    companion object {
        const val START_BUTTON_LABEL = "Start"
        const val STOP_BUTTON_LABEL = "Stop"
        const val COUNTDOWN_WINDOW_TITLE = "Odliczanie"
        const val COUNTDOWN_SETUP_WINDOW_TITLE = "Parametry odliczania"
    }
}

fun Window.asFxWindow(): FxWindow =
    when (this) {
        is Stage -> FxWindow(this)
        else -> throw IllegalArgumentException("Only Java FX stage can be converted to FxWindow")
    }
