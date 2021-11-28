package pl.klodnicka.church.countdown.adapters

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextClosedEvent
import tornadofx.Component
import tornadofx.EventContext
import tornadofx.FX
import tornadofx.FXEvent
import tornadofx.FXEventRegistration
import tornadofx.runLater
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

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
    private val delayedExecutor: ScheduledExecutorService,
    private val internalEventBus: InternalTornadoFxEventBusComponent
) : ApplicationListener<ContextClosedEvent> {

    fun <T : Message> publish(event: T) {
        internalEventBus.fire(event)
    }

    fun <T : Message> subscribe(messageType: KClass<T>, delayMs: Long = 0, subscriber: (T) -> Unit) {
        if (delayMs > 0) {
            internalEventBus.subscribe(messageType) {
                delayedExecutor.schedule({ runLater { subscriber(it) } }, delayMs, TimeUnit.MILLISECONDS)
            }
        } else {
            internalEventBus.subscribe(messageType) { subscriber(it) }
        }
    }

    override fun onApplicationEvent(event: ContextClosedEvent) {
        delayedExecutor.shutdown()
    }
}

class InternalTornadoFxEventBusComponent : Component() {

    @Suppress("UNCHECKED_CAST")
    fun <T : FXEvent> subscribe(type: KClass<T>, times: Number? = null, action: EventContext.(T) -> Unit) {
        val registration = FXEventRegistration(type, this, times?.toLong(), action as EventContext.(FXEvent) -> Unit)
        subscribedEvents.compute(type) { _, list ->
            val newList = if (list != null) ArrayList(list) else ArrayList()
            newList.add(registration)
            newList
        }
        FX.eventbus.subscribe(type, scope, registration)
    }
}
