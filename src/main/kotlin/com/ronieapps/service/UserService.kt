package com.ronieapps.service

import com.ronieapps.model.User
import com.ronieapps.repository.UserRepository
import java.util.UUID

class UserService(private val userRepository: UserRepository) {
    fun findById(id: String): User? =
        userRepository.findById(UUID.fromString(id))

    fun findByUsername(username: String): User? =
        userRepository.findByUsername(username)

    fun create(user: User): User? {
        val foundUser = userRepository.findByUsername(user.username)

        return if (foundUser == null) {
            userRepository.save(user)
            user
        } else null
    }
}