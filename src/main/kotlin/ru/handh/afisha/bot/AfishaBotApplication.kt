package ru.handh.afisha.bot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories("ru.handh.afisha.bot.repository")
open class AfishaBotApplication

fun main(args: Array<String>) {
    SpringApplication.run(AfishaBotApplication::class.java, *args)
}
