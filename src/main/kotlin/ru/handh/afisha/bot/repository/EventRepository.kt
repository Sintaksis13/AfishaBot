package ru.handh.afisha.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.handh.afisha.bot.domain.Event

interface EventRepository : JpaRepository<Event, Long> {
}
