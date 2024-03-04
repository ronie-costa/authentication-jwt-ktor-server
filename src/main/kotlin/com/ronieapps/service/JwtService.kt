package com.ronieapps.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.ronieapps.routing.request.UserRequest
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtService(
    private val application: Application,
    private val userService: UserService
) {
    private val audience = getConfigProperty("jwt.audience")
    private val issuer = getConfigProperty("jwt.issuer")
    private val secret = getConfigProperty("jwt.secret")
    val realm = getConfigProperty("jwt.realm")

    val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun createJwtToken(username: String): Map<String, String>? {
        val foundUser = userService.findByUsername(username)

        return if (foundUser != null) {
            val expiresAt = Date(System.currentTimeMillis() + 60000000)
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("uid", foundUser.uid.toString())
                .withClaim("username", foundUser.username)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secret))
            mapOf(
                "expiresAt" to expiresAt.toString(),
                "token" to token
            )
        } else null
    }

    fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val username = extractUsername(credential)
        val foundUser = username?.let(userService::findByUsername)

        return foundUser?.let {
            if (audienceMatches(credential)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(audience)

    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()

    private fun getConfigProperty(path: String) =
        application.environment.config.property(path).toString()
}