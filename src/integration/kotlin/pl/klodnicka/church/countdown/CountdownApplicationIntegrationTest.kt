package pl.klodnicka.church.countdown

import javafx.scene.control.Labeled
import org.junit.jupiter.api.Test
import org.testfx.assertions.api.Assertions.assertThat

class CountdownApplicationIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `label contains hello world text`() {
        val label: Labeled = lookup(".label").query()

        assertThat(label).hasText("Hello, JavaFX!")
    }
}
