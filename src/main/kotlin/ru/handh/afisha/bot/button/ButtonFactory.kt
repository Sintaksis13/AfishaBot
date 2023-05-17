package ru.handh.afisha.bot.button

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.domain.Callback.Companion.CANCEL_REGISTRATION
import ru.handh.afisha.bot.domain.Callback.Companion.EVENT_INFO
import ru.handh.afisha.bot.domain.Callback.Companion.HELP
import ru.handh.afisha.bot.domain.Callback.Companion.MY_EVENTS
import ru.handh.afisha.bot.domain.Callback.Companion.REGISTRATION
import ru.handh.afisha.bot.domain.Callback.Companion.START
import ru.handh.afisha.bot.domain.Callback.Companion.UPCOMING_EVENTS
import ru.handh.afisha.bot.message.MessageHandler
import ru.handh.afisha.bot.message.MessageHandler.Companion.CANCEL_REGISTER_MESSAGE
import ru.handh.afisha.bot.message.MessageHandler.Companion.REGISTER_MESSAGE
import ru.handh.afisha.bot.service.CallbackService

@Component
class ButtonFactory(
    private val callbackService: CallbackService,
    private val messageHandler: MessageHandler
) {
    private val startMenu: InlineKeyboardMarkup
    private val adminStartMenu: InlineKeyboardMarkup

    private val upcomingEventsButton = InlineKeyboardButton("Ближайшие события")
    private val myEventsButton = InlineKeyboardButton("Мои события")
    private val helpButton = InlineKeyboardButton("Подсказка")
    private val startButton = InlineKeyboardButton("Начальное меню")

    init {
        upcomingEventsButton.callbackData = callbackService.prepareCallbackAsString(callbackType = UPCOMING_EVENTS)
        myEventsButton.callbackData = callbackService.prepareCallbackAsString(callbackType = MY_EVENTS)
        helpButton.callbackData = callbackService.prepareCallbackAsString(callbackType = HELP)
        startButton.callbackData = callbackService.prepareCallbackAsString(callbackType = START)

        val keyboard = listOf(
            listOf(upcomingEventsButton, myEventsButton),
            listOf(helpButton, startButton),
        )

        startMenu = prepareMenu(keyboard)
        adminStartMenu = prepareMenu(listOf(listOf(upcomingEventsButton)))
    }

    fun createEventMenu(events: List<Event>): InlineKeyboardMarkup {
        val buttons = mutableListOf<List<InlineKeyboardButton>>()
        for (event in events) {
            val button = InlineKeyboardButton(messageHandler.prepareEventShortDescription(event))
            button.callbackData = callbackService.prepareCallbackAsString(
                event.id,
                EVENT_INFO
            )
            buttons.add(listOf(button))
        }

        buttons.add(listOf(startButton))

        return prepareMenu(buttons)
    }

    fun createEventInfoMenu(event: Event, alreadyRegistered: Boolean): InlineKeyboardMarkup {
        val buttons = mutableListOf<List<InlineKeyboardButton>>()
        if (alreadyRegistered) {
            val registerButton = InlineKeyboardButton(CANCEL_REGISTER_MESSAGE)
            registerButton.callbackData = callbackService.prepareCallbackAsString(
                event.id,
                CANCEL_REGISTRATION
            )
            buttons.add(listOf(registerButton))
        } else {
            val registerButton = InlineKeyboardButton(REGISTER_MESSAGE)
            registerButton.callbackData = callbackService.prepareCallbackAsString(
                event.id,
                REGISTRATION
            )
            buttons.add(listOf(registerButton))
        }

        buttons.add(listOf(startButton))
        buttons.add(listOf(upcomingEventsButton))

        return prepareMenu(buttons)
    }

    private fun prepareMenu(buttons: List<List<InlineKeyboardButton>>): InlineKeyboardMarkup {
        val menu = InlineKeyboardMarkup()
        menu.keyboard = buttons
        return menu
    }

    fun getStartMenu(): InlineKeyboardMarkup {
        return startMenu
    }

    fun getAdminStartMenu(): InlineKeyboardMarkup {
        return adminStartMenu
    }
}
