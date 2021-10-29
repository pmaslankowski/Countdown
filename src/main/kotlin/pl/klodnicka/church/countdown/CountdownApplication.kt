package pl.klodnicka.church.countdown

import tornadofx.*

import javafx.application.Application
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.View
import kotlin.reflect.KClass

@SpringBootApplication
class CountdownApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(FxCountdownApplication::class.java, *args)
        }
    }
}

class FxCountdownApplication : App(MainView::class) {
    private lateinit var context: ConfigurableApplicationContext

    override fun init() {
        context = SpringApplicationBuilder(CountdownApplication::class.java).run()

        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = context.getBean(type.java)
            override fun <T : Any> getInstance(type: KClass<T>, name: String): T = context.getBean(name, type.java)
        }
    }

    override fun stop() {
        super.stop()
        context.close()
    }
}

class MainView : View("MainView") {

    override val root = vbox {
        label { text = "Hello, JavaFX!" }
    }
}