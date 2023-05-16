package ru.handh.afisha.bot.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Registration(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @Column(name = "user_name")
    val userName: String,

    @Column(name = "chat_id")
    val chatId: Long,

    @Column(name = "event_id")
    val eventId: Long
)
