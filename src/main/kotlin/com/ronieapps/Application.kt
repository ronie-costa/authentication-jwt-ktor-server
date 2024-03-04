package com.ronieapps

import com.ronieapps.plugins.*
import com.ronieapps.repository.UserRepository
import com.ronieapps.routing.configureRouting
import com.ronieapps.service.JwtService
import com.ronieapps.service.UserService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val userRepository = UserRepository()
    val userService = UserService(userRepository)
    val jwtService = JwtService(this, userService)

    configureSerialization()
    configureSecurity(jwtService)
    configureRouting(userService, jwtService)
}
