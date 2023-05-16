package ru.handh.afisha.bot.service

import org.springframework.stereotype.Service
import ru.handh.afisha.bot.domain.Registration
import ru.handh.afisha.bot.repository.RegistrationRepository

@Service
class RegistrationService(
    private val registrationRepository: RegistrationRepository
) {
    fun register(
        userName: String,
        chatId: Long,
        eventId: Long
    ) {
        registrationRepository.save(Registration(
            userName = userName,
            eventId = eventId,
            chatId = chatId
        ))
    }

    fun cancelRegistration(
        userName: String,
        eventId: Long
    ) {
        registrationRepository.findByUserNameAndEventId(userName, eventId).ifPresent {
            registrationRepository.deleteById(it.id!!)
        }
    }

    fun getRegistrationsForUser(userName: String): List<Registration> {
        return registrationRepository.findAllByUserName(userName)
    }

    fun getRegistrationsCountForEvent(eventId: Long): Int {
        return registrationRepository.findEventRegistrationsCount(eventId)
    }

    fun isUserRegistered(eventId: Long, userName: String): Boolean =
        registrationRepository.findByUserNameAndEventId(
            userName,
            eventId
        ).isPresent
}
