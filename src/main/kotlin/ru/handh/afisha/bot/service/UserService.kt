package ru.handh.afisha.bot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.handh.afisha.bot.domain.UserData
import ru.handh.afisha.bot.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,

    @Value("afisha.bot.admin.login")
    private val adminLogin: String,

    @Value("afisha.bot.admin.password")
    private val adminPassword: String
) {
    fun isUserExists(userName: String): Boolean {
        return userRepository.findByUserName(userName).isPresent
    }

    fun isAdminLoggedIn(userName: String): Boolean {
        val foundUser = userRepository.findByUserName(userName)
        return foundUser.isPresent && foundUser.get().isAdmin
    }

    fun getFullNameByUserName(userName: String): String {
        return userRepository.findByUserName(userName).map(UserData::fullName)
            .orElse("Незнакомый человек")
    }

    fun saveUser(userName: String, userFullName: String) {
        userRepository.save(
            UserData(
                userName = userName,
                fullName = userFullName,
                isAdmin = false
            )
        )
    }

    fun saveAdmin(userName: String) {
        userRepository.save(
            UserData(
                userName = userName,
                fullName = adminLogin,
                isAdmin = true
            )
        )
    }

    fun isCredentialsValid(credentials: String): Boolean {
        val splitCredentials = credentials.split(" ")
        return splitCredentials[0] == adminLogin && splitCredentials[1] == adminPassword
    }
}
