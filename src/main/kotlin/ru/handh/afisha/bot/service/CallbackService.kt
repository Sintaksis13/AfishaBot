package ru.handh.afisha.bot.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import ru.handh.afisha.bot.domain.Callback

@Service
class CallbackService {
    private val mapper = ObjectMapper()

    fun prepareCallbackAsString(eventId: Long? = null, callbackType: String): String {
        return mapper.writeValueAsString(
            Callback(
                callbackType,
                eventId
            )
        )
    }

    fun parseCallback(callback: String): Callback {
        return mapper.readValue(callback, Callback::class.java)
    }
}
