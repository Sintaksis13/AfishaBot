package ru.handh.afisha.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.handh.afisha.bot.domain.Registration
import java.util.Optional

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun findAllByUserName(userName: String): List<Registration>

    fun findAllByEventId(eventId: Long): List<Registration>

    fun findByUserNameAndEventId(userName: String, eventId: Long): Optional<Registration>

    @Query("SELECT COUNT(id) FROM Registration WHERE eventId = :eventId")
    fun findEventRegistrationsCount(eventId: Long): Int

    fun deleteAllByEventId(eventId: Long)
}
