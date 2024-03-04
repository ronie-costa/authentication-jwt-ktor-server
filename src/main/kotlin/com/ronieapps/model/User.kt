package com.ronieapps.model

import java.util.*

data class User(
    val uid: UUID,
    val username: String,
    val password: String
)
