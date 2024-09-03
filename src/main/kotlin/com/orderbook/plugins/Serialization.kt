package com.orderbook.plugins

import com.orderbook.OrderBookRepositoryImpl
import com.orderbook.OrderBookService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        val repository = OrderBookRepositoryImpl()
        val service = OrderBookService(repository)
        get("/json/kotlinx-serialization") {
                call.respond(mapOf("hello" to "world"))
            }
        get("/orderbook"){
            call.respond(service.getOrderBook())
        }
    }
}
