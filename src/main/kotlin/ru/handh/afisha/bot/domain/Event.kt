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
    val name: String,
    val description: String?,
    @Column(name = "time")
    val dateTime: LocalDateTime,
    @Column(name = "seats")
    val availableSeats: Int
)
