package ru.handh.afisha.bot.service

import org.springframework.stereotype.Service
import ru.handh.afisha.bot.domain.UserData
import ru.handh.afisha.bot.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun isUserExists(userName: String): Boolean {
        return userRepository.findByUserName(userName).isPresent
    }

    fun getFullNameByUserName(userName: String): String {
        return userRepository.findByUserName(userName).map(UserData::fullName)
            .orElse("Незнакомый человек")
    }

    fun saveUser(userName: String, userFullName: String) {
        userRepository.save(
            UserData(
                userName = userName,
                fullName = userFullName
            )
        )
    }
}
