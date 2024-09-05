package com.orderbook.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.orderbook.OrderBookService
import com.orderbook.models.LimitOrder
import com.orderbook.models.User
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import org.slf4j.event.Level
import java.util.*

fun Application.configureSerialization() {
    val service = OrderBookService()

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
    install(RequestValidation) {
        validate<LimitOrder> { order ->
            val errors = mutableListOf<String>()
            if (order.customerOrderId.isBlank()) {
                errors.add("Customer Order ID is required")
            }
            if (!service.isCustomerOrderIdUnique(order.customerOrderId))
                errors.add("Customer Order ID must be unique")

            if (order.pair.isBlank()) errors.add("Pair is required")

            if(order.price.isBlank()) errors.add("Price is required")
            else if (order.price.toDouble() <= 0) errors.add("Invalid Price")

            if (order.quantity.isBlank()) errors.add("Quantity is required")
            else if (order.quantity.toDouble() <= 0) errors.add("Invalid quantity")

            if (order.side !in listOf("BUY", "SELL")) errors.add("Side must be either BUY or SELL")
            if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
        }
    }
    routing {
        post("/login") {
            val request = call.receive<User>()

            if (request.username != "admin" || request.password != "admin") {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim(request.username, request.password)
                .withExpiresAt(Date(System.currentTimeMillis() +10 * 60 * 1000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(mapOf("token" to token))
        }
        authenticate {
            get("/orders/order-book") {
                call.respond(service.getOrderBook())
            }

            post("/orders/limit") {
                try {
                    val request = call.receive<LimitOrder>()
                    val response = service.addLimitOrder(request)

                    call.respond(HttpStatusCode.OK, mapOf("id" to response))

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: SerializationException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }

            }
            get("/orders/trade-history") {
                val tradeHistory = service.getTradeHistory()
                call.respond(HttpStatusCode.OK, tradeHistory)
            }

            get("/orders/open") {
                val openOrders = service.getOpenOrders()
                call.respond(HttpStatusCode.OK, openOrders)
            }

            get("orders/orderId/{orderId}") {
                val orderId = call.parameters["orderId"] ?: ""
                val order = service.getOrderByOrderId(orderId)
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Order not found"))
                }
            }

            get("/orders/all") {
                val orders = service.getOrders()
                call.respond(HttpStatusCode.OK, orders)
            }

        }
    }

}
