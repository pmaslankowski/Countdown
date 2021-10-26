package pl.klodnicka.church.countdown

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CountdownApplication

fun main(args: Array<String>) {
	runApplication<CountdownApplication>(*args)
}
