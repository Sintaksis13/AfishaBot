package ru.handh.afisha.bot.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.handh.afisha.bot.domain.UserData
import java.util.Optional

interface UserRepository : JpaRepository<UserData, Long> {
    fun findByUserName(userName: String): Optional<UserData>
}
