package com.ronieapps.routing

import com.ronieapps.model.User
import com.ronieapps.routing.request.UserRequest
import com.ronieapps.routing.response.UserResponse
import com.ronieapps.service.JwtService
import com.ronieapps.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import kotlin.math.exp

fun Application.configureRouting(userService: UserService, jwtService: JwtService) {
    routing {
        get("/") {
            call.respond(
                "Authentication JWT \n" +
                        "\n" +
                        " - Requisições \n" +
                        "\n" +
                        "Criar Conta: /api/auth/create \n" +
                        " - header: (Content-Type) application/json \n" +
                        " - body: username: String, password: String \n" +
                        "\n" +
                        "Logar Usuário: /api/auth/login \n" +
                        " - header: (Content-Type) application/json \n" +
                        " - body: username: String, password: String \n" +
                        "\n" +
                        "Request User: /api/user-request \n" +
                        " - header: (Authentication) bearer 'token'"
            )
        }
        route("/api/auth") {
            post("/create") {
                val userRequest = call.receive<UserRequest>()

                val createUser = userService.create(userRequest.toModel())
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val jwtMap = jwtService.createJwtToken(userRequest.username)
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val token = jwtMap["token"].toString()
                val expiresAt = jwtMap["expiresAt"].toString()

                call.respond(createUser.toResponse(token, expiresAt))
            }
            post("/login") {
                val userRequest = call.receive<UserRequest>()

                val foundUser = userService.findByUsername(userRequest.username)
                    ?: return@post call.respondText(
                        "usuário não existe",
                        status = HttpStatusCode.BadRequest
                    )

                if (foundUser.password != userRequest.password)
                    call.respondText("senha invalida", status = HttpStatusCode.BadRequest)

                val jwtMap = jwtService.createJwtToken(userRequest.username)
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val token = jwtMap["token"].toString()
                val expiresAt = jwtMap["expiresAt"].toString()

                call.respond(foundUser.toResponse(token, expiresAt))
            }
        }
        route("/api/token") {
            post("/{username?}") {
                val username = call.parameters["username"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val jwtMap = jwtService.createJwtToken(username)
                    ?: return@post call.respondText(
                        "usuário não existe ou senha invalida",
                        status = HttpStatusCode.BadRequest
                    )

                val token = jwtMap["token"].toString()
                val expiresAt = jwtMap["expiresAt"].toString()

                call.respond(
                    hashMapOf(
                        "token" to token,
                        "expires_at" to expiresAt
                    )
                )
            }
        }
        route("/api/user-request") {
            authenticate("auth-jwt") {
                get {
                    val principal = call.principal<JWTPrincipal>()

                    val uid = principal!!.payload.getClaim("uid").asString()
                    val username = principal.payload.getClaim("username").asString()
                    val expiresAt = principal.expiresAt.toString()

                    call.respond(
                        hashMapOf(
                            "uid" to uid.toString(),
                            "username" to username.toString(),
                            "expires_at" to expiresAt
                        )
                    )
                }
            }
        }
    }
}

private fun UserRequest.toModel(): User =
    User(
        uid = UUID.randomUUID(),
        username = this.username,
        password = this.password
    )

private fun User.toResponse(token: String, expiresAt: String): UserResponse =
    UserResponse(
        uid = this.uid,
        idToken = token,
        expiresAt = expiresAt,
        username = this.username
    )