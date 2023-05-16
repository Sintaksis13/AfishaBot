package ru.handh.afisha.bot.message

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.domain.Callback.Companion.HELP
import ru.handh.afisha.bot.domain.Callback.Companion.MY_EVENTS
import ru.handh.afisha.bot.domain.Callback.Companion.START
import ru.handh.afisha.bot.domain.Callback.Companion.UPCOMING_EVENTS
import java.time.format.DateTimeFormatter

@Component
class MessageHandler(
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
) {
    fun prepareMessage(
        chatId: Long,
        text: String,
        menu: InlineKeyboardMarkup?
    ): SendMessage {
        val message = SendMessage()

        message.chatId = chatId.toString()
        message.text = text
        message.replyMarkup = menu

        return message
    }

    fun prepareEventDescription(event: Event) = """
                "${event.name}"
                
                ${event.description}
                
                Дата проведения: ${dateFormatter.format(event.dateTime)}
                Количество мест на событии: ${event.availableSeats}
            """.trimIndent()

    fun prepareEventShortDescription(event: Event) = """
        ${event.name}
        (${dateFormatter.format(event.dateTime)})
        """.trimIndent()

    fun prepareGreetingMessage(userName: String) = "Привет, ${userName}, добро пожаловать в Бот Афиши!"

    fun prepareWelcomeMessage(fullName: String) = "Привет, ${fullName}, добро пожаловать в Бот Афиши! Введи свои ФИО:"

    companion object {
        const val START_DESCRIPTION = "Начальное меню"
        const val UPCOMING_EVENTS_DESCRIPTION = "Ближайшие события"
        const val MY_EVENTS_DESCRIPTION = "Мои события"
        const val HELP_DESCRIPTION = "Подсказка"
        const val HELP_MESSAGE =
            """
                Здесь вы можете:
                Посмотреть ближайшие события: $UPCOMING_EVENTS
                Посмотреть список моих событий: $MY_EVENTS
                Открыть подсказку: $HELP
                Вернуться в начальное меню: $START
            """
        const val REGISTER_MESSAGE = "Зарегистрироваться на событие"
        const val CANCEL_REGISTER_MESSAGE = "Отменить регистрацию"
        const val EMPTY_EVENTS_MESSAGE = "К сожалению, в данный момент доступных событий нет :("
        const val ERROR_MESSAGE = "Что-то пошло не так... Попробуйте снова немного позже :("
    }
}
