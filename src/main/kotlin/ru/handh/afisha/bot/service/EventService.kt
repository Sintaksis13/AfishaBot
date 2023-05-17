package ru.handh.afisha.bot.service

import org.springframework.stereotype.Service
import ru.handh.afisha.bot.domain.Event
import ru.handh.afisha.bot.repository.EventRepository
import java.time.LocalDateTime
import java.util.*

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

    fun createEvent(
        name: String,
        description: String?,
        dateTime: LocalDateTime,
        availableSeats: Int
    ): Event {
        return eventRepository.save(
            Event(
                name = name,
                description = description,
                dateTime = dateTime,
                availableSeats = availableSeats
            )
        )
    }

    fun updateEvent(event: Event): Event {
       return eventRepository.save(event)
    }

    fun deleteEvent(eventId: Long) {
        eventRepository.deleteById(eventId)
    }
}
