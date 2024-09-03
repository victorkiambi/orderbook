package com.orderbook.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.orderbook.OrderBookService
import com.orderbook.models.Order
import com.orderbook.models.User
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.Level
import java.util.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()
    install(Authentication) {
        jwt {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }
    routing {
        val service = OrderBookService()
        get("/json/kotlinx-serialization") {
                call.respond(mapOf("hello" to "world"))
            }
        post("/login"){
            //check username and password
            val request = call.receive<User>()

            if (request.username != "admin" || request.password != "admin") {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim(request.username, request.password)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(mapOf("token" to token))
        }
        authenticate {
            get("/orders/order-book") {
                call.respond(service.getOrderBook())
            }

            post("/orders/limit") {
                val request = call.receive<Order>()
                val response = service.addOrder(request)

                if (response != null) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.OK, "No match")
                }

            }
            get("/orders/trade-history") {
                val tradeHistory = service.getTradeHistory()
                call.respond(HttpStatusCode.OK, tradeHistory)
            }


        }
    }
}
