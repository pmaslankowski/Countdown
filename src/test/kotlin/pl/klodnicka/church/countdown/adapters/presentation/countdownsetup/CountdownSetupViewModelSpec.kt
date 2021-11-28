package pl.klodnicka.church.countdown.adapters.presentation.countdownsetup

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import pl.klodnicka.church.countdown.FakeDisplay
import pl.klodnicka.church.countdown.FakeDisplayProvider
import pl.klodnicka.church.countdown.FakeDisplays
import pl.klodnicka.church.countdown.adapters.EventBus
import pl.klodnicka.church.countdown.adapters.presentation.DurationParser
import pl.klodnicka.church.countdown.adapters.presentation.InvalidDurationException
import pl.klodnicka.church.countdown.adapters.presentation.countdown.StopCountdownCommand
import pl.klodnicka.church.countdown.domain.min

internal class CountdownSetupViewModelSpec {

    private val displayProvider = FakeDisplayProvider()
    private val fakeDisplays: FakeDisplays = FakeDisplays(displayProvider)
    private val countdownRunner: CountdownRunner = mockk(relaxed = true)
    private val eventBus: EventBus = mockk(relaxed = true)
    private val durationParser = DurationParser()

    @Test
    fun `has correct properties after initialization`() {
        // given
        val primaryDisplay = FakeDisplay(name = "primary-display")
        val otherDisplay = FakeDisplay(name = "other-display")
        fakeDisplays.thereAreDisplays(primaryDisplay, otherDisplay)

        // when
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // then
        assertThat(viewModel.duration).isEqualTo("05:00")
        assertThat(viewModel.selectedDisplay).isEqualTo(primaryDisplay)
        assertThat(viewModel.availableDisplays).containsExactly(primaryDisplay, otherDisplay)
        assertThat(viewModel.startButtonEnabled).isTrue
        assertThat(viewModel.stopButtonEnabled).isFalse
    }

    @Test
    fun `starts countdown on the selected display`() {
        // given
        val primaryDisplay = FakeDisplay(name = "primary-display")
        fakeDisplays.thereAreDisplays(primaryDisplay)

        // and
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.startCountdown()

        // then
        verify {
            countdownRunner.startCountdown(5.min, primaryDisplay)
        }
    }

    @Test
    fun `throws an exception when starting countdown with invalid duration`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)
        viewModel.duration = "invalid-value"

        // expect
        assertThatThrownBy { viewModel.startCountdown() }
            .isInstanceOf(InvalidDurationException::class.java)
    }

    @Test
    fun `publishes application closed event when countdown setup closed`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.countdownSetupClosed()

        // then
        verify {
            eventBus.publish(ApplicationClosedEvent)
        }
    }

    @Test
    fun `publishes stop countdown command when stop requested`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.stopCountdown()

        // then
        verify {
            eventBus.publish(StopCountdownCommand)
        }
    }

    @Test
    fun `validates duration correctly when duration is valid`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)
        viewModel.duration = "00:50"

        // when
        val result = viewModel.validateDuration()

        // then
        assertThat(result).isTrue
        assertThat(viewModel.startButtonEnabled).isTrue
    }

    @Test
    fun `validates duration correctly when duration is invalid`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)
        viewModel.duration = "invalid-duration"

        // when
        val result = viewModel.validateDuration()

        // then
        assertThat(result).isFalse
    }

    @Test
    fun `disables start button when duration is invalid after validation`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.duration = "invalid-duration"
        viewModel.validateDuration()

        // then
        assertThat(viewModel.startButtonEnabled).isFalse
    }

    @Test
    fun `enables start button when duration becomes valid again`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.duration = "invalid-duration"
        viewModel.validateDuration()

        viewModel.duration = "00:10"
        viewModel.validateDuration()

        // then
        assertThat(viewModel.startButtonEnabled).isTrue
    }

    @Test
    fun `disables start button and enables stop button when countdown starts`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.onCountdownStarted()

        // then
        assertThat(viewModel.startButtonEnabled).isFalse
        assertThat(viewModel.stopButtonEnabled).isTrue
    }

    @Test
    fun `enables start button and disables stop button when countdown finishes`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)

        // when
        viewModel.onCountdownFinished()

        // then
        assertThat(viewModel.startButtonEnabled).isTrue
        assertThat(viewModel.stopButtonEnabled).isFalse
    }

    @Test
    fun `validation leaves start button disabled when countdown is running`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)
        viewModel.onCountdownStarted()

        // when
        viewModel.duration = "invalid-duration"
        viewModel.validateDuration()

        viewModel.duration = "00:10"
        viewModel.validateDuration()

        // then
        assertThat(viewModel.startButtonEnabled).isFalse
    }

    @Test
    fun `leaves start button disabled when countdown finishes`() {
        // given
        val viewModel = CountdownSetupViewModel(displayProvider, durationParser, countdownRunner, eventBus)
        viewModel.onCountdownStarted()

        // when
        viewModel.duration = "invalid-duration"
        viewModel.validateDuration()

        viewModel.onCountdownFinished()

        // then
        assertThat(viewModel.startButtonEnabled).isFalse
    }
}
