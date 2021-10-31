package pl.klodnicka.church.countdown.adapters

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent
import tornadofx.Component
import tornadofx.FXEvent
import tornadofx.runLater
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Configuration
class EventBusConfiguration {

    @Bean
    fun eventBus() = EventBus(
        Executors.newScheduledThreadPool(
            1,
            ThreadFactoryBuilder()
                .setNameFormat("event-bus-delayed-executor-thread-%d")
                .build()
        ),
        InternalTornadoFxEventBusComponent()
    )
}

abstract class Message : FXEvent()
abstract class Event : Message()
abstract class Command : Message()

class EventBus(
    val delayedExecutor: ScheduledExecutorService,
    val internalEventBus: InternalTornadoFxEventBusComponent
) : ApplicationListener<ContextClosedEvent> {

    fun <T : Message> publish(event: T) {
        internalEventBus.fire(event)
    }

    inline fun <reified T : Message> subscribe(delayMs: Long = 0, noinline subscriber: (T) -> Unit) {
        if (delayMs > 0) {
            internalEventBus.subscribe<T> {
                delayedExecutor.schedule({ runLater { subscriber(it) } }, delayMs, TimeUnit.MILLISECONDS)
            }
        } else {
            internalEventBus.subscribe<T> { subscriber(it) }
        }
    }

    override fun onApplicationEvent(event: ContextClosedEvent) {
        delayedExecutor.shutdown()
    }
}

class InternalTornadoFxEventBusComponent : Component()
