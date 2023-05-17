package ru.handh.afisha.bot.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,
    var name: String,
    var description: String?,
    @Column(name = "time")
    var dateTime: LocalDateTime,
    @Column(name = "seats")
    var availableSeats: Int
)
