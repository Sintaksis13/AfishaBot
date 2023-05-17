package ru.handh.afisha.bot.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.afisha.bot.button.ButtonFactory
import ru.handh.afisha.bot.command.AdminCommandHandler
import ru.handh.afisha.bot.message.MessageHandler
import ru.handh.afisha.bot.service.CallbackService
import ru.handh.afisha.bot.service.UserService

@Component
class AfishaBotAdminClient(
    @Value("\${afisha.bot.admin.token}")
    botToken: String,

    @Value("\${afisha.bot.admin.name}")
    private val botName: String,

    callbackService: CallbackService,
    userService: UserService,
    messageHandler: MessageHandler,
    buttonFactory: ButtonFactory
) : TelegramLongPollingBot(botToken) {
    private val commandHandler: AdminCommandHandler = AdminCommandHandler(
        this,
        callbackService,
        userService,
        messageHandler,
        buttonFactory
    )

    override fun getBotUsername() = botName

    override fun onUpdateReceived(update: Update?) = commandHandler.handelUpdate(update)
}
