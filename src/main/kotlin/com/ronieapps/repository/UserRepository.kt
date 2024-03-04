package com.ronieapps.repository

import com.ronieapps.model.User
import java.util.*

class UserRepository {
    private val users = mutableListOf<User>()

    fun findAll(): List<User> = users

    fun findById(uid: UUID): User? =
        users.firstOrNull { it.uid == uid }

    fun findByUsername(username: String): User? =
        users.firstOrNull { it.username == username }

    fun save(user: User): Boolean =
        users.add(user)
}