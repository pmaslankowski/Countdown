package pl.klodnicka.church.countdown

import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Labeled
import javafx.stage.Stage
import javafx.stage.Window
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.testfx.assertions.api.Assertions.assertThat
import org.testfx.util.WaitForAsyncUtils.waitFor
import pl.klodnicka.church.countdown.adapters.FxWindow
import pl.klodnicka.church.countdown.domain.FixedClock
import pl.klodnicka.church.countdown.domain.and
import pl.klodnicka.church.countdown.domain.min
import pl.klodnicka.church.countdown.domain.sec
import java.lang.IllegalArgumentException
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Suppress("FunctionName")
class CountdownApplicationIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var fakeDisplays: FakeDisplays

    @Autowired
    private lateinit var fixedClock: FixedClock

    @BeforeEach
    fun setup() {
        fakeDisplays.thereAreDisplays(FakeDisplay())
    }

    @Test
    fun `runs countdown and closes the window when it finishes`() {
        runApplication()

        clickStartCountdown()

        assertCountdownIsShowing("05:00")
        assertStartButton(disabled = true)
        assertStopButton(disabled = false)

        fixedClock.advance(4.min and 30.sec)
        assertCountdownIsShowing("00:30")

        fixedClock.advance(30.sec)

        assertCountdownClosed()
        assertStartButton(disabled = false)
        assertStopButton(disabled = true)
    }

    @Test
    fun `shows countdown on a correct display`() {
        val secondDisplay = FakeDisplay(name = "Second display", minX = 1000.0, minY = 0.0)
        fakeDisplays.thereAreDisplays(primaryDisplay = FakeDisplay(), secondDisplay)

        runApplication()

        selectDisplay(secondDisplay)
        clickStartCountdown()

        assertCountdownIsShownOn(secondDisplay)
    }

    @Test
    fun `runs another countdown when the first one has finished`() {
        runApplication()

        clickStartCountdown()
        fixedClock.advance(5.min)

        assertCountdownClosed()

        clickStartCountdown()
        assertCountdownIsShowing("05:00")
    }

    @Test
    fun `stops the countdown when the countdown setup window has been closed`() {
        runApplication()

        clickStartCountdown()
        fixedClock.advance(1.min)

        close(countdownSetupWindow())

        assertCountdownClosed()
    }

    @Test
    fun `stops the countdown by clicking stop button`() {
        runApplication()

        clickStartCountdown()
        fixedClock.advance(1.min)

        clickStopCountdown()

        assertCountdownClosed(timeoutMs = 1000)
        assertStartButton(disabled = false)
        assertStopButton(disabled = true)
    }

    private fun clickStartCountdown() {
        interact {
            countdownSetupWindow().requestFocus()
        }
        assertStartButton(disabled = false)
        clickOn(START_BUTTON_LABEL)
    }

    private fun clickStopCountdown() {
        interact {
            countdownSetupWindow().requestFocus()
        }
        assertStopButton(disabled = false)
        clickOn(STOP_BUTTON_LABEL)
    }

    private fun selectDisplay(display: FakeDisplay) {
        val comboBox: ComboBox<FakeDisplay> = lookup<ComboBox<FakeDisplay>> { true }.queryComboBox()
        interact {
            comboBox.selectionModel.select(display)
        }
    }

    private fun assertCountdownIsShowing(timeLeft: String) {
        val label = countdownLabel()
        waitFor(messageOnFail = "Countdown is not showing") {
            label.text == timeLeft
        }
    }

    private fun assertCountdownClosed(timeoutMs: Long = 10000) {
        waitFor(messageOnFail = "Countdown has not been closed", timeoutMs = timeoutMs) {
            listWindows().none { it is Stage && it.title?.equals(COUNTDOWN_WINDOW_TITLE) ?: false }
        }
    }

    private fun assertStartButton(disabled: Boolean) {
        waitFor(
            messageOnFail = "Start button is ${toState(!disabled)} but was expected to be ${toState(disabled)}",
            timeoutMs = 1000
        ) {
            lookup(START_BUTTON_LABEL).query<Button>().isDisabled == disabled
        }
    }

    private fun toState(disabled: Boolean) =
        if (disabled) {
            "disabled"
        } else {
            "enabled"
        }

    private fun assertStopButton(disabled: Boolean) {
        waitFor(
            messageOnFail = "Stop button is ${toState(!disabled)} but was expected to be ${toState(disabled)}",
            timeoutMs = 1000
        ) {
            lookup(STOP_BUTTON_LABEL).query<Button>().isDisabled == disabled
        }
    }

    private fun assertCountdownIsShownOn(display: FakeDisplay) {
        assertThat(display.isShowing(countdownWindow().asFxWindow())).isTrue
    }

    private fun countdownLabel(): Labeled = from(rootNode(countdownWindow())).lookup(".label").query()

    private fun countdownWindow(): Window = window(COUNTDOWN_WINDOW_TITLE)

    private fun countdownSetupWindow(): Window = window(COUNTDOWN_SETUP_WINDOW_TITLE)

    private fun close(window: Window) {
        interact { (window as Stage).close() }
    }

    private fun waitFor(messageOnFail: String, timeoutMs: Long = 100, condition: Callable<Boolean>) {
        try {
            waitFor(timeoutMs, TimeUnit.MILLISECONDS, condition)
        } catch (ex: TimeoutException) {
            throw AssertionError(messageOnFail)
        }
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
