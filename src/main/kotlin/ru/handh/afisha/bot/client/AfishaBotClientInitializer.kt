package ru.handh.afisha.bot.client

import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Component
class AfishaBotClientInitializer(private val client: AfishaBotClient) {
    private val logger = LoggerFactory.getLogger(AfishaBotClientInitializer::class.java)

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        try {
            val api = TelegramBotsApi(DefaultBotSession::class.java)
            api.registerBot(client)
        } catch (e: TelegramApiException) {
            logger.error("Error occurred: ${e.message}")
        }
    }
}
