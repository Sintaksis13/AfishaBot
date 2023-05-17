package ru.handh.afisha.bot.command

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import ru.handh.afisha.bot.button.ButtonFactory
import ru.handh.afisha.bot.client.AfishaBotClient
import ru.handh.afisha.bot.domain.Callback
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.message.MessageHandler
import ru.handh.afisha.bot.message.MessageHandler.Companion.CREATE_EVENT_MESSAGE
import ru.handh.afisha.bot.message.MessageHandler.Companion.LOGIN_FAILED_MESSAGE
import ru.handh.afisha.bot.message.MessageHandler.Companion.LOGIN_MESSAGE
import ru.handh.afisha.bot.message.MessageHandler.Companion.START_DESCRIPTION
import ru.handh.afisha.bot.message.MessageHandler.Companion.UPCOMING_EVENTS_DESCRIPTION
import ru.handh.afisha.bot.message.MessageHandler.Companion.UPDATE_EVENT_MESSAGE
import ru.handh.afisha.bot.message.MessageSender
import ru.handh.afisha.bot.service.CallbackService
import ru.handh.afisha.bot.service.EventService
import ru.handh.afisha.bot.service.RegistrationService
import ru.handh.afisha.bot.service.UserService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AdminCommandHandler(
    client: TelegramLongPollingBot,
    private val afishaBotClient: AfishaBotClient,
    callbackService: CallbackService,
    private val userService: UserService,
    private val registrationService: RegistrationService,
    private val messageHandler: MessageHandler,
    private val eventService: EventService,
    private val buttonFactory: ButtonFactory,
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
) : CommandHandler(
    client,
    callbackService,
    userService,
    registrationService,
    messageHandler,
    eventService,
    buttonFactory
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

    override fun handleCommand(
        text: String,
        chatId: Long,
        userName: String
    ) {
        val isNotLoggedIn = !userService.isAdminLoggedIn(userName)
        if (isNotLoggedIn) {
            if (Callback.START != text) {
                if (userService.isCredentialsValid(text)) {
                    userService.saveAdmin(userName)
                    sendStartMenu(chatId)
                } else {
                    sendLoginFailedMessage(chatId)
                    sendLoginMessage(chatId)
                }
            } else {
                sendLoginMessage(chatId)
            }
        } else {
            if (isEventManagingCommand(text)) {
                handleEventManaging(text, chatId)
            } else {
                when (text) {
                    Callback.START -> sendStartMenu(chatId)
                    Callback.UPCOMING_EVENTS -> sendUpcomingEvents(chatId)
                }
            }
        }
    }

    override fun handleCommand(
        callback: Callback,
        chatId: Long,
        userName: String
    ) {
        when (callback.type) {
            Callback.START -> sendStartMenu(chatId)
            Callback.UPCOMING_EVENTS -> sendUpcomingEvents(chatId)
            Callback.CREATE_EVENT -> sendCreateEvent(chatId)
            Callback.EVENT_INFO -> sendEventInfo(chatId, callback.eventId!!)
            Callback.DELETE_EVENT -> deleteEvent(chatId, callback.eventId!!)
            Callback.UPDATE_EVENT -> sendUpdateEvent(chatId)
            Callback.SHOW_PARTICIPANTS -> sendParticipants(chatId, callback.eventId!!)
        }
    }

    private fun sendParticipants(chatId: Long, eventId: Long) {
        val registrations = registrationService.getRegistrationsForEvent(eventId)
        val userNames = registrations.map { it.userName }.toList()
        val users = userService.getUsersByUserNames(userNames)
        val messageText = messageHandler.prepareParticipantsMessage(users)
        val message = messageHandler.prepareMessage(
            chatId,
            messageText,
            null
        )

        messageSender.sendMessage(message)
    }

    private fun sendUpdateEvent(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            UPDATE_EVENT_MESSAGE,
            null
        )

        messageSender.sendMessage(message)
    }

    private fun updateEvent(event: Event) {
        val registrations = registrationService.getRegistrationsForEvent(event.id!!)

        registrations.forEach {
            afishaBotClient.commandHandler.sendEventChanged(it.chatId, event)
        }
    }

    private fun deleteEvent(chatId: Long, eventId: Long) {
        val registrations = registrationService.getRegistrationsForEvent(eventId)
        val eventOpt = eventService.getEventById(eventId)

        eventOpt.ifPresentOrElse(
            { event ->
                registrations.forEach {
                    afishaBotClient.commandHandler.sendEventChanged(it.chatId, event, isDeleted = true)
                }
            },
            {
                messageSender.sendErrorMessage(chatId)
            }
        )

        eventService.deleteEvent(eventId)
        registrationService.deleteRegistrationsForEvent(eventId)
    }

    private fun sendEventInfo(
        chatId: Long,
        eventId: Long? = null,
        event: Event? = null
    ) {
        var eventOpt = Optional.ofNullable(event)
        if (event == null) {
            eventOpt = eventService.getEventById(eventId!!)
        }

        eventOpt.ifPresentOrElse(
            {
                messageSender.sendMessage(
                    messageHandler.prepareMessage(
                        chatId,
                        messageHandler.prepareEventDescription(it, true),
                        buttonFactory.createEventInfoAdmin(it)
                    )
                )
            },
            {
                messageSender.sendErrorMessage(chatId)
            }
        )
    }

    private fun handleEventManaging(text: String, chatId: Long) {
        val values = text.split("\n")
        var command = values.first()
        var eventId: Long? = null
        if (command.contains(UPDATE_EVENT_COMMAND)) {
            val commandAndEventId = command.split(" ")
            eventId = commandAndEventId.last().toLong()
            command = commandAndEventId.first()
        }

        when (command) {
            CREATE_EVENT_COMMAND -> {
                val event = eventService.createEvent(
                    values[1],
                    values[2],
                    LocalDateTime.parse(values[3], dateFormatter),
                    values[4].toInt()
                )

                sendEventInfo(chatId, event = event)
            }
            UPDATE_EVENT_COMMAND -> {
                if (eventId == null) {
                    messageSender.sendErrorMessage(chatId)
                }

                val eventOpt = eventService.getEventById(eventId!!)
                if (eventOpt.isEmpty) {
                    messageSender.sendErrorMessage(chatId)
                }

                val event = eventOpt.get()
                event.name = values[1]
                event.description = values[2]
                event.dateTime = LocalDateTime.parse(values[3], dateFormatter)
                event.availableSeats = values[4].toInt()

                val updatedEvent = eventService.updateEvent(event)
                updateEvent(updatedEvent)
                sendEventInfo(chatId, event = event)
            }
        }
    }

    private fun isEventManagingCommand(text: String) = EVENT_COMMANDS_LIST.any { text.contains(it) }

    private fun sendCreateEvent(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            CREATE_EVENT_MESSAGE,
            null
        )

        messageSender.sendMessage(message)
    }

    private fun sendStartMenu(chatId: Long) {
        val message = messageHandler.prepareMessage(
            chatId,
            START_DESCRIPTION,
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

    private fun sendUpcomingEvents(chatId: Long) {
        val upcomingEvents = eventService.getUpcomingEvents()
        val eventMenu = buttonFactory.createEventMenu(upcomingEvents, true)
        val message = messageHandler.prepareMessage(
            chatId,
            if (upcomingEvents.isEmpty()) MessageHandler.EMPTY_EVENTS_MESSAGE
            else UPCOMING_EVENTS_DESCRIPTION,
            eventMenu
        )

        messageSender.sendMessage(message)
    }

    companion object {
        const val CREATE_EVENT_COMMAND = "create"
        const val UPDATE_EVENT_COMMAND = "update"

        val ADMIN_COMMANDS = listOf(
            BotCommand(Callback.START, START_DESCRIPTION),
            BotCommand(Callback.UPCOMING_EVENTS, UPCOMING_EVENTS_DESCRIPTION),
        )
        val EVENT_COMMANDS_LIST = listOf(
            CREATE_EVENT_COMMAND,
            UPDATE_EVENT_COMMAND
        )
    }
}
