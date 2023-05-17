package ru.handh.afisha.bot.command

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.handh.afisha.bot.button.ButtonFactory
import ru.handh.afisha.bot.domain.Callback
import ru.handh.afisha.bot.message.MessageHandler
import ru.handh.afisha.bot.message.MessageHandler.Companion.LOGIN_FAILED_MESSAGE
import ru.handh.afisha.bot.message.MessageHandler.Companion.LOGIN_MESSAGE
import ru.handh.afisha.bot.message.MessageSender
import ru.handh.afisha.bot.service.CallbackService
import ru.handh.afisha.bot.service.UserService

class AdminCommandHandler(
    client: TelegramLongPollingBot,
    private val callbackService: CallbackService,
    private val userService: UserService,
    private val messageHandler: MessageHandler,
    private val buttonFactory: ButtonFactory
) {
    private val log = LoggerFactory.getLogger(AdminCommandHandler::class.java)

    private val messageSender = MessageSender(
        client,
        messageHandler,
        buttonFactory
    )

    init {
        try {
            client.execute(SetMyCommands(ADMIN_COMMANDS, BotCommandScopeDefault(), null))
        } catch (e: TelegramApiException) {
            log.error("Error occurred during commands sending: ${e.message}")
        }
    }

    fun handelUpdate(update: Update?) {
        if (update?.hasMessage() == true) {
            val command = update.message

            handleCommand(
                command.text,
                command.chatId,
                command.from.userName
            )
        } else if (update?.hasCallbackQuery() == true) {
            val callback = update.callbackQuery

            handleCommand(
                callbackService.parseCallback(callback.data),
                callback.message.chatId,
                callback.from.userName
            )
        }
    }

    private fun handleCommand(
        text: String,
        chatId: Long,
        userName: String
    ) {
        val isAdminLoggedId = !userService.isAdminLoggedIn(userName)
        if (isAdminLoggedId) {
            if (Callback.START != text) {
                if (userService.isCredentialsValid(text)) {
                    userService.saveAdmin(
                        userName
                    )
                    sendStartMenu(chatId)
                } else {
                    sendLoginFailedMessage(chatId)
                    sendLoginMessage(chatId)
                }
            } else {
                sendLoginMessage(chatId)
            }
        } else {

        }
    }

    private fun handleCommand(
        callback: Callback,
        chatId: Long,
        userName: String
    ) {

    }

    private fun sendStartMenu(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            "Смари",
            buttonFactory.getAdminStartMenu()
        )

        messageSender.sendMessage(message)
    }

    private fun sendLoginMessage(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            LOGIN_MESSAGE,
            null
        )

        messageSender.sendMessage(message)
    }

    private fun sendLoginFailedMessage(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            LOGIN_FAILED_MESSAGE,
            null
        )

        messageSender.sendMessage(message)
    }

    companion object {
        val ADMIN_COMMANDS = listOf(
            BotCommand(Callback.START, MessageHandler.START_DESCRIPTION),
            BotCommand(Callback.UPCOMING_EVENTS, MessageHandler.UPCOMING_EVENTS_DESCRIPTION),
        )
    }
}
