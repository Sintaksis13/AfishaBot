package ru.handh.afisha.bot.service

import org.springframework.stereotype.Service
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.repository.EventRepository
import java.util.Optional

@Service
class EventService(
    private val eventRepository: EventRepository
) {
    fun getUpcomingEvents(): List<Event> {
        return eventRepository.findAll()
    }

    fun getEventById(eventId: Long): Optional<Event> {
        return eventRepository.findById(eventId)
    }

    fun getEventsByIds(eventIds: List<Long>): List<Event> {
        return eventRepository.findAllById(eventIds)
    }
}
