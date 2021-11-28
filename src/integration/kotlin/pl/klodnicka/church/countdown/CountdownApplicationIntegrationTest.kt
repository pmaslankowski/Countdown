package pl.klodnicka.church.countdown

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import pl.klodnicka.church.countdown.domain.FixedClock
import pl.klodnicka.church.countdown.domain.and
import pl.klodnicka.church.countdown.domain.min
import pl.klodnicka.church.countdown.domain.sec

@Suppress("FunctionName")
class CountdownApplicationIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var fakeDisplays: FakeDisplays

    @Autowired
    private lateinit var robot: CountdownRobot

    @Autowired
    private lateinit var fixedClock: FixedClock

    @BeforeEach
    fun setup() {
        fakeDisplays.thereAreDisplays(FakeDisplay())
    }

    @Test
    fun `runs countdown and closes the window when it finishes`() {
        runApplication()

        robot.startCountdown()

        robot.assertCountdownIsShowing("05:00")
        robot.assertStartButtonStateIs(enabled = false)
        robot.assertStopButtonStateIs(enabled = true)

        fixedClock.advance(4.min and 30.sec)
        robot.assertCountdownIsShowing("00:30")

        fixedClock.advance(30.sec)

        robot.assertCountdownClosed()
        robot.assertStartButtonStateIs(enabled = true)
        robot.assertStopButtonStateIs(enabled = false)
    }

    @Test
    fun `shows countdown on a correct display`() {
        val secondDisplay = FakeDisplay(name = "Second display", minX = 1000.0, minY = 0.0)
        fakeDisplays.thereAreDisplays(primaryDisplay = FakeDisplay(), secondDisplay)

        runApplication()

        robot.selectDisplay(secondDisplay)
        robot.startCountdown()

        robot.assertCountdownIsShownOn(secondDisplay)
    }

    @Test
    fun `runs another countdown when the first one has finished`() {
        runApplication()

        robot.startCountdown()
        fixedClock.advance(5.min)

        robot.assertCountdownClosed()

        robot.startCountdown()
        robot.assertCountdownIsShowing("05:00")
    }

    @Test
    fun `stops the countdown when the countdown setup window has been closed`() {
        runApplication()

        robot.startCountdown()
        fixedClock.advance(1.min)

        robot.closeCountdownSetupWindow()

        robot.assertCountdownClosed()
    }

    @Test
    fun `stops the countdown by clicking stop button`() {
        runApplication()

        robot.startCountdown()
        fixedClock.advance(1.min)

        robot.stopCountdown()

        robot.assertCountdownClosed(timeoutMs = 1000)
        robot.assertStartButtonStateIs(enabled = true)
        robot.assertStopButtonStateIs(enabled = false)
    }
}
