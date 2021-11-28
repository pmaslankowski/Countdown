package pl.klodnicka.church.countdown.adapters

import javafx.stage.Screen
import javafx.stage.Stage
import org.springframework.stereotype.Component
import pl.klodnicka.church.countdown.domain.Display
import pl.klodnicka.church.countdown.domain.DisplayProvider
import pl.klodnicka.church.countdown.domain.Window
import tornadofx.runLater

@Component
class FxDisplayProvider : DisplayProvider {

    override fun getAvailableDisplays(): List<FxDisplay> =
        Screen.getScreens()
            .mapIndexed { index, screen -> FxDisplay("Ekran ${index + 1}", screen) }

    override fun getPrimaryDisplay(): FxDisplay {
        val primaryScreen = Screen.getPrimary()
        return getAvailableDisplays().find { it.screen == primaryScreen }
            ?: throw CannotDeterminePrimaryDisplayException()
    }
}

data class FxDisplay(
    override val name: String,
    val screen: Screen,
) : Display {

    override val width: Double = screen.bounds.width
    override val height: Double = screen.bounds.height
    override val minX: Double = screen.visualBounds.minX
    override val minY: Double = screen.visualBounds.minY

    override fun label(): String = "$name [$width x $height]"

    override fun showFullScreenWindow(window: Window) {
        window.showFullScreenOn(this)
    }

    override fun toString(): String = label()
}

data class FxWindow(val stage: Stage) : Window {

    override fun showFullScreenOn(display: Display) {
        with(stage) {
            x = display.minX
            y = display.minY
            runLater {
                isFullScreen = true
            }
        }
    }
}

class CannotDeterminePrimaryDisplayException : RuntimeException(
    "Błąd podczas wykrywania głównego ekranu: nie znaleziono głównego ekranu wśród dostępnych ekranów."
)
