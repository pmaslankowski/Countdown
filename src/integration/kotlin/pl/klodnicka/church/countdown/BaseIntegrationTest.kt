package pl.klodnicka.church.countdown

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

@SpringBootTest
class BaseIntegrationTest : ApplicationTest() {

    companion object {

        @JvmStatic
        @BeforeAll
        fun setupMonocle() {
            System.setProperty("testfx.robot", "glass")
            System.setProperty("testfx.headless", "true")
            System.setProperty("prism.order", "sw")
            System.setProperty("prism.text", "t2k")
            System.setProperty("java.awt.headless", "true")
        }
    }

    @BeforeEach
    fun runApplication() {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication(::FxCountdownApplication)
        FxToolkit.showStage()
        WaitForAsyncUtils.waitForFxEvents(100)
    }

    @AfterEach
    fun stopApplication() {
        FxToolkit.cleanupStages()
    }
}
