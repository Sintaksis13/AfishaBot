package ru.handh.afisha.bot.domain

class Callback(
    val type: String? = null,
    val eventId: Long? = null
) {
    override fun toString(): String {
        return "Callback(command=$type, eventId=$eventId)"
    }

    companion object {
        const val START = "/start"
        const val HELP = "/help"
        const val UPCOMING_EVENTS = "/upcoming"
        const val MY_EVENTS = "/my"
        const val EVENT_INFO = "/info"
        const val REGISTRATION = "/reg"
        const val CANCEL_REGISTRATION = "/cancelreg"
    }
}
