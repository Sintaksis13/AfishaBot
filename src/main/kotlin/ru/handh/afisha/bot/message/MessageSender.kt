package ru.handh.afisha.bot.message

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.handh.afisha.bot.button.ButtonFactory

class MessageSender(
    private val client: TelegramLongPollingBot,
    private val messageHandler: MessageHandler,
    private val buttonFactory: ButtonFactory
) {
    private val log = LoggerFactory.getLogger(MessageSender::class.java)

    fun sendErrorMessage(chatId: Long) {
        val errorMessage = messageHandler.prepareMessage(
            chatId,
            MessageHandler.ERROR_MESSAGE,
            buttonFactory.getStartMenu()
        )

        sendMessage(errorMessage)
    }

    fun sendMessage(message: SendMessage) {
        try {
            client.execute(message)
            log.info("Message sent, $message")
        } catch (e: TelegramApiException) {
            log.error("Error occurred while message=$message sending: ${e.message}")
        }
    }
}
