package pl.klodnicka.church.countdown

import javafx.application.Application
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import org.testfx.api.FxRobot
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import pl.klodnicka.church.countdown.domain.Clock
import pl.klodnicka.church.countdown.domain.FixedClock

@SpringBootTest
@ContextConfiguration(classes = [IntegrationTestConfiguration::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseIntegrationTest(
    private val headless: Boolean = true
) : FxRobot() {

    @Autowired
    private lateinit var context: ConfigurableApplicationContext

    private var currentlyRunningApplication: Application? = null

    @BeforeAll
    fun setupMonocle() {
        if (headless) {
            turnOnHeadlessMode()
        }
    }

    private fun turnOnHeadlessMode() {
        System.setProperty("testfx.robot", "glass")
        System.setProperty("testfx.headless", "true")
        System.setProperty("prism.order", "sw")
        System.setProperty("prism.text", "t2k")
        System.setProperty("java.awt.headless", "true")
    }

    fun runApplication() {
        IntegrationTestHelper.setTestApplicationContext(context)
        FxToolkit.registerPrimaryStage()
        currentlyRunningApplication = FxToolkit.setupApplication(::FxCountdownApplication)
        FxToolkit.showStage()
        WaitForAsyncUtils.waitForFxEvents(100)
    }

    @AfterEach
    fun stopApplication() {
        currentlyRunningApplication?.let {
            FxToolkit.cleanupApplication(it)
            FxToolkit.cleanupStages()

            // Cleaning the remaining UI events (e.g. a mouse press that is still waiting for a mouse release)
            // Not cleaning these events may have side-effects on the next UI tests
            release(*arrayOfNulls<KeyCode>(0))
            release(*arrayOfNulls<MouseButton>(0))
            // Required to wait for the end of the UI events processing
            WaitForAsyncUtils.waitForFxEvents()
        }
    }
}

@Configuration
@ComponentScan
class IntegrationTestConfiguration {

    @Bean
    @Primary
    fun displayProvider(): FakeDisplayProvider = FakeDisplayProvider()

    @Bean
    fun fakeDisplays(displayProvider: FakeDisplayProvider): FakeDisplays = FakeDisplays(displayProvider)

    @Bean
    @Primary
    fun fixedClock(): Clock = FixedClock()
}
