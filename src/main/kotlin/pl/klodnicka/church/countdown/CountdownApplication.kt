package pl.klodnicka.church.countdown

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext

@SpringBootApplication
class CountdownApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(FxCountdownApplication::class.java, *args)
        }
    }
}

class FxCountdownApplication : Application() {
    private lateinit var applicationContext: ConfigurableApplicationContext

    override fun init() {
        applicationContext = SpringApplicationBuilder(CountdownApplication::class.java).run()
    }

    override fun start(stage: Stage) {
        val l = Label("Hello, JavaFX!")
        val scene = Scene(StackPane(l), 640.0, 480.0)
        stage.scene = scene
        stage.show()
    }

    override fun stop() {
        applicationContext.close()
        Platform.exit()
    }
}
