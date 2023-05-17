package ru.handh.afisha.bot.command

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.handh.afisha.bot.button.ButtonFactory
import ru.handh.afisha.bot.client.AfishaBotClient
import ru.handh.afisha.bot.domain.Callback
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.domain.Registration
import ru.handh.afisha.bot.message.MessageHandler
import ru.handh.afisha.bot.message.MessageSender
import ru.handh.afisha.bot.service.CallbackService
import ru.handh.afisha.bot.service.EventService
import ru.handh.afisha.bot.service.RegistrationService
import ru.handh.afisha.bot.service.UserService

open class CommandHandler(
    client: TelegramLongPollingBot,
    private val callbackService: CallbackService,
    private val userService: UserService,
    private val registrationService: RegistrationService,
    private val messageHandler: MessageHandler,
    private val eventService: EventService,
    private val buttonFactory: ButtonFactory
) {
    private val log = LoggerFactory.getLogger(AfishaBotClient::class.java)

    private val messageSender = MessageSender(
        client,
        messageHandler,
        buttonFactory
    )

    init {
        try {
            client.execute(SetMyCommands(COMMANDS, BotCommandScopeDefault(), null))
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

    open fun handleCommand(
        text: String,
        chatId: Long,
        userName: String
    ) {
        val firstEnter = !userService.isUserExists(userName)
        if (firstEnter) {
            if (Callback.START != text) {
                userService.saveUser(userName, text)
                sendStartMenu(chatId, userName)
            } else {
                sendWelcomeMessage(chatId, userName)
            }
        } else {
            when (text) {
                Callback.START -> sendStartMenu(chatId, userName)
                Callback.HELP -> sendHelpText(chatId)
                Callback.UPCOMING_EVENTS -> sendUpcomingEvents(chatId, userName)
                Callback.MY_EVENTS -> sendMyEvents(chatId, userName)
                else -> sendHelpText(chatId)
            }
        }
    }

    open fun handleCommand(
        callback: Callback,
        chatId: Long,
        userName: String
    ) {
        when (callback.type) {
            Callback.START -> sendStartMenu(chatId, userName)
            Callback.HELP -> sendHelpText(chatId)
            Callback.EVENT_INFO -> sendEventDetails(chatId, callback.eventId, userName)
            Callback.UPCOMING_EVENTS -> sendUpcomingEvents(chatId, userName)
            Callback.MY_EVENTS -> sendMyEvents(chatId, userName)
            Callback.REGISTRATION -> {
                registrationService.register(userName, chatId, callback.eventId!!)
                sendMyEvents(chatId, userName)
            }

            Callback.CANCEL_REGISTRATION -> {
                registrationService.cancelRegistration(userName, callback.eventId!!)
                sendMyEvents(chatId, userName)
            }
        }
    }

    private fun sendWelcomeMessage(chatId: Long, userName: String) {
        val message = messageHandler.prepareMessage(
            chatId,
            messageHandler.prepareWelcomeMessage(userName),
            null
        )

        messageSender.sendMessage(message)
    }

    private fun sendStartMenu(chatId: Long, fullName: String) {
        val message = messageHandler.prepareMessage(
            chatId,
            messageHandler.prepareGreetingMessage(userService.getFullNameByUserName(fullName)),
            buttonFactory.getStartMenu()
        )

        messageSender.sendMessage(message)
    }

    private fun sendHelpText(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            MessageHandler.HELP_MESSAGE,
            buttonFactory.getStartMenu()
        )

        messageSender.sendMessage(message)
    }

    private fun sendUpcomingEvents(chatId: Long, userName: String) {
        //TODO: сделать лимит/оффсет реализацию для доступных событий
        val registrationsForUser = registrationService.getRegistrationsForUser(userName)
        val upcomingEvents = eventService.getUpcomingEvents()
        val availableUpcomingEvent = upcomingEvents.filter {
            it.id !in registrationsForUser.map(Registration::eventId) || userService.isAdminLoggedIn(userName)
        }.filter {
            it.availableSeats > registrationService.getRegistrationsCountForEvent(it.id!!)
                    || userService.isAdminLoggedIn(userName)
        }

        val eventMenu = buttonFactory.createEventMenu(availableUpcomingEvent)
        val message = messageHandler.prepareMessage(
            chatId,
            if (availableUpcomingEvent.isEmpty()) MessageHandler.EMPTY_EVENTS_MESSAGE else MessageHandler.UPCOMING_EVENTS_DESCRIPTION,
            eventMenu
        )

        messageSender.sendMessage(message)
    }

    private fun sendMyEvents(chatId: Long, userName: String) {
        val message = messageHandler.prepareMessage(
            chatId,
            MessageHandler.MY_EVENTS_DESCRIPTION,
            buttonFactory.createEventMenu(
                eventService.getEventsByIds(
                    registrationService.getRegistrationsForUser(userName)
                        .map(Registration::eventId)
                )
            )
        )

        messageSender.sendMessage(message)
    }

    private fun sendEventDetails(chatId: Long, eventId: Long?, userName: String) {
        val event = eventService.getEventById(eventId ?: throw IllegalArgumentException())
        event.ifPresentOrElse(
            {
                messageSender.sendMessage(
                    messageHandler.prepareMessage(
                        chatId,
                        messageHandler.prepareEventDescription(it),
                        buttonFactory.createEventInfoMenu(it, registrationService.isUserRegistered(eventId, userName))
                    )
                )
            },
            {
                messageSender.sendErrorMessage(chatId)
            }
        )
    }

    fun sendEventChanged(chatId: Long, event: Event, isDeleted: Boolean = false) {
        val messageText = if (isDeleted) {
            MessageHandler.EVENT_DELETED_MESSAGE
        } else {
            messageHandler.prepareEventChanged(event.name)
        }

        val message = messageHandler.prepareMessage(
            chatId,
            messageText,
            buttonFactory.createChangedEventMenu(event, isDeleted)
        )

        messageSender.sendMessage(message)
    }

    companion object {
        val COMMANDS = listOf(
            BotCommand(Callback.UPCOMING_EVENTS, MessageHandler.UPCOMING_EVENTS_DESCRIPTION),
            BotCommand(Callback.MY_EVENTS, MessageHandler.MY_EVENTS_DESCRIPTION),
            BotCommand(Callback.HELP, MessageHandler.HELP_DESCRIPTION),
            BotCommand(Callback.START, MessageHandler.START_DESCRIPTION),
        )
    }
}
