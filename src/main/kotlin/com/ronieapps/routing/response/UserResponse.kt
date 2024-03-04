package com.ronieapps.routing.response

import com.ronieapps.util.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserResponse(
    @Serializable(UUIDSerializer::class)
    val uid: UUID,
    @SerialName("id_token")
    val idToken: String,
    @SerialName("expires_at")
    val expiresAt: String,
    val username: String
)
