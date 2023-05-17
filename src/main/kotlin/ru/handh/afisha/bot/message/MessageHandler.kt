package ru.handh.afisha.bot.message

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.domain.Callback.Companion.HELP
import ru.handh.afisha.bot.domain.Callback.Companion.MY_EVENTS
import ru.handh.afisha.bot.domain.Callback.Companion.START
import ru.handh.afisha.bot.domain.Callback.Companion.UPCOMING_EVENTS
import ru.handh.afisha.bot.domain.UserData
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

    fun prepareEventDescription(event: Event, isAdmin: Boolean = false) = """
                "${event.name}" ${if (isAdmin) event.id else ""}
                
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

    fun prepareEventChanged(eventName: String) = "Событие '$eventName' изменилось! Посмотрите новые данные"

    fun prepareParticipantsMessage(users: List<UserData>): String {
        val builder = StringBuilder()
        users.forEach {
            builder.append(it.userName + " " + it.fullName + "\n")
        }

        return builder.toString()
    }

    companion object {
        const val START_DESCRIPTION = "Начальное меню"
        const val UPCOMING_EVENTS_DESCRIPTION = "Ближайшие события"
        const val MY_EVENTS_DESCRIPTION = "Мои события"
        const val HELP_DESCRIPTION = "Подсказка"
        const val CREATE_EVENT_DESCRIPTION = "Создать событие"
        const val UPDATE_EVENT_DESCRIPTION = "Изменить событие"
        const val DELETE_EVENT_DESCRIPTION = "Удалить событие"
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
        const val LOGIN_MESSAGE = "Привет, введи логин и пароль через пробел, вот так: login password"
        const val LOGIN_FAILED_MESSAGE = "Неверный логин или пароль, попробуй снова"
        const val EVENT_DELETED_MESSAGE = "К сожалению, событие отменено :( Посмотрите список ближайших событий"
        const val CREATE_EVENT_MESSAGE = """
            Чтобы создать новое событие необходимо:
            1) Первой строкой указать ключевое слово create
            2) Второй строкой указать название события
            3) Третьей строкой указать описание события (в случае отсутствия описания - оставить пустую строку)
            4) Четвертой строкой указать дату и время события (в формате "2023-05-30 17:00", без ковычек)
            5) Пятой строкой указать количество доступных для события мест
            
            Пример:
            create
            Новое событие
            Описание события
            2023-06-01 19:00
            30
            
            Еще пример, без описания:
            create
            Новое событие 2
            
            2023-07-02 13:00
            100
        """

        const val UPDATE_EVENT_MESSAGE = """
            Чтобы изменить событие необходимо:
            1) Первой строкой указать ключевое слово update и id события
            2) Второй строкой указать название события
            3) Третьей строкой указать описание события (в случае отсутствия описания - оставить пустую строку)
            4) Четвертой строкой указать дату и время события (в формате "2023-05-30 17:00", без ковычек)
            5) Пятой строкой указать количество доступных для события мест
            Если какое-то поле не меняется - просто скопировать существующее значение
            
            Пример:
            update 1
            Новое событие
            Описание события
            2023-06-01 19:00
            30
            
            Еще пример, без описания:
            update 2
            Новое событие 2
            
            2023-07-02 13:00
            100
        """
    }
}
