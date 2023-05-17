package ru.handh.afisha.bot.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.handh.afisha.bot.button.ButtonFactory
import ru.handh.afisha.bot.command.CommandHandler
import ru.handh.afisha.bot.message.MessageHandler
import ru.handh.afisha.bot.service.CallbackService
import ru.handh.afisha.bot.service.EventService
import ru.handh.afisha.bot.service.RegistrationService
import ru.handh.afisha.bot.service.UserService

@Component
class AfishaBotClient(
    @Value("\${afisha.bot.token}")
    botToken: String,

    @Value("\${afisha.bot.name}")
    private val botName: String,

    buttonFactory: ButtonFactory,
    eventService: EventService,
    callbackService: CallbackService,
    registrationService: RegistrationService,
    messageHandler: MessageHandler,
    userService: UserService
) : TelegramLongPollingBot(botToken) {
    val commandHandler: CommandHandler = CommandHandler(
        this,
        callbackService,
        userService,
        registrationService,
        messageHandler,
        eventService,
        buttonFactory
    )

    override fun getBotUsername() = botName

    override fun onUpdateReceived(update: Update?) = commandHandler.handelUpdate(update)
}
