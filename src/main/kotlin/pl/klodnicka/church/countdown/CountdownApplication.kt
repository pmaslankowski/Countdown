package pl.klodnicka.church.countdown

import javafx.application.Application
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import pl.klodnicka.church.countdown.adapters.logger
import pl.klodnicka.church.countdown.adapters.presentation.CountdownSetupView
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import java.lang.IllegalStateException
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

class FxCountdownApplication : App(CountdownSetupView::class) {
    private lateinit var context: ConfigurableApplicationContext

    override fun init() {
        context = initContext()
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = context.getBean(type.java)
            override fun <T : Any> getInstance(type: KClass<T>, name: String): T = context.getBean(name, type.java)
        }
    }

    private fun initContext(): ConfigurableApplicationContext {
        if (IntegrationTestHelper.isRunForTests()) {
            log.info("Application is initialized using test application context shared with integration tests")
            return IntegrationTestHelper.getTestApplicationContext()
        }
        return createNewApplicationContext()
    }

    private fun createNewApplicationContext(): ConfigurableApplicationContext =
        SpringApplicationBuilder(CountdownApplication::class.java).run()

    override fun stop() {
        super.stop()
        if (!IntegrationTestHelper.isRunForTests()) {
            context.close()
        } else {
            log.info("Application is run using test application context and consequently the context is left to be closed by the testing framework")
        }
    }

    companion object {
        val log by logger()
    }
}

object IntegrationTestHelper {

    private var context: ConfigurableApplicationContext? = null

    fun isRunForTests(): Boolean = context != null

    fun getTestApplicationContext() =
        context ?: throw IllegalStateException("Application has not been run for integration tests")

    fun setTestApplicationContext(context: ConfigurableApplicationContext) {
        this.context = context
    }
}
