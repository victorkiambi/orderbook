package com.orderbook

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testOrderBook() = testApplication {
        client.get("/orderbook").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("{\"Asks\":[],\"Bids\":[],\"LastChange\":\"\",\"SequenceNumber\":1}", bodyAsText())
        }
    }

    @Test
    fun testAddBuyOrder() = testApplication {
        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                    "allowMargin": "true",
                    "customerOrderId": "1",
                    "pair": "BTCUSD",
                    "postOnly": true,
                    "price": "10000",
                    "quantity": "1",
                    "side": "BUY",
                    "timeInForce": "GTC"
                }
                """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Order placed: Order(allowMargin=true, customerOrderId=1, pair=BTCUSD, postOnly=true, price=10000, quantity=1, side=BUY, timeInForce=GTC)", bodyAsText())
        }
    }

    @Test
    fun testAddSellOrder() = testApplication {
        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                    "allowMargin": "true",
                    "customerOrderId": "1",
                    "pair": "BTCUSD",
                    "postOnly": true,
                    "price": "10000",
                    "quantity": "1",
                    "side": "SELL",
                    "timeInForce": "GTC"
                }
                """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Order placed: Order(allowMargin=true, customerOrderId=1, pair=BTCUSD, postOnly=true, price=10000, quantity=1, side=SELL, timeInForce=GTC)", bodyAsText())
        }
    }
}
