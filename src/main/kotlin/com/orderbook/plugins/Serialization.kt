package com.orderbook.plugins

import com.orderbook.OrderBookService
import com.orderbook.models.Order
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        val service = OrderBookService()
        get("/json/kotlinx-serialization") {
                call.respond(mapOf("hello" to "world"))
            }
        get("/orderbook"){
            call.respond(service.getOrderBook())
        }

        post("/orders/limit"){
            val request = call.receive<Order>()
            service.addOrder(request)
            call.respond(HttpStatusCode.OK, "Order placed: $request")
        }

    }
}
