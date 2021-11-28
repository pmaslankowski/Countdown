package pl.klodnicka.church.countdown.domain

interface Display {
    val name: String
    val width: Double
    val height: Double
    val minX: Double
    val minY: Double

    fun label(): String

    fun showFullScreenWindow(window: Window)
}

interface Window {

    fun showFullScreenOn(display: Display)
}

interface DisplayProvider {

    fun getAvailableDisplays(): List<Display>

    fun getPrimaryDisplay(): Display
}
