package com.orderbook

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.orderbook.models.OrderBook
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testLogin() = testApplication {
        client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                    "username": "admin",
                    "password": "admin"
                }
                """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testLoginFail() = testApplication {
        client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                    "username": "admin",
                    "password": "wrong"
                }
                """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testInvalidToken() = testApplication {
        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer invalid_token")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }
    @Test
    fun testExpiredToken() = testApplication {
        // Generate an expired token
        val expiredToken = JWT.create()
            .withAudience("http://localhost:8080/orders")
            .withIssuer("http://localhost:8080")
            .withClaim("username", "admin")
            .withExpiresAt(Date(System.currentTimeMillis() - 1000))
            .sign(Algorithm.HMAC256("secret"))

        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $expiredToken")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testMissingToken() = testApplication {
        client.get("/orders/order-book").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    private suspend fun loginAndGetToken(client: HttpClient): String {
        val tokenResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                    "username": "admin",
                    "password": "admin"
                }
                """.trimIndent()
            )
        }

        // Check if the login request was successful
        assertEquals(HttpStatusCode.OK, tokenResponse.status)
        val token = Json.decodeFromString<JsonObject>(tokenResponse.bodyAsText())["token"]?.jsonPrimitive?.content
        assertNotNull(token)
        return token
    }

    @Test
    fun testAuthenticatedOrderBook() = testApplication {
        val token = loginAndGetToken(client)

        val orderBookResponse = client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // Assert that the response status is OK (200)
        assertEquals(HttpStatusCode.OK, orderBookResponse.status )
    }


    @Test
    fun testAddBuyOrder() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }
    }

    @Test
    fun testAddSellOrder() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }
    }

    @Test
    fun testAddOrderNoMatch() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
            //assert that there is no trade
            val responseBody = Json.decodeFromString<JsonObject>(bodyAsText())
            val id = responseBody["id"]?.jsonPrimitive?.content
            assertNotNull(id)
        }
    }

    @Test
    fun testAddMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "2",
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
            //assert that there is a trade
            val responseBody = Json.decodeFromString<JsonObject>(bodyAsText())
            val id = responseBody["id"]?.jsonPrimitive?.content
            assertNotNull(id)
        }
    }

    @Test
    fun testInvalidOrderParameters() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "12345",
                "pair": "BTCUSD",
                "postOnly": true,
                "price": "1000", // Invalid price
                "quantity": "0.03",
                "side": "SELL",
                "timeInForce": "GTC"
            }
            """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errors = Json.decodeFromString<JsonObject>(bodyAsText())["error"]
            assertNotNull(errors)
        }
    }

    @Test
    fun testInvalidJsonFormat() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("{ invalid json }")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testMissingRequiredFields() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "1",
                "pair": "BTCUSD",
                "postOnly": true,
                "price": "10000"
                // Missing quantity, side, and timeInForce
            }
            """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            val errors = Json.decodeFromString<JsonObject>(bodyAsText())["error"]
            assertNotNull(errors)
        }
    }

    @Test
    fun testTradeHistory() = testApplication {
        val token = loginAndGetToken(client)

        client.get("/orders/trade-history") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testTradeHistoryNoToken() = testApplication {
        client.get("/orders/trade-history").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testTradeHistoryInvalidToken() = testApplication {
        client.get("/orders/trade-history") {
            header(HttpHeaders.Authorization, "Bearer invalid_token")
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testTradeHistoryWithMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "2",
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
        }

        client.get("/orders/trade-history") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val tradeHistory = Json.decodeFromString<List<JsonObject>>(bodyAsText())
            assert(tradeHistory.isNotEmpty())
            assertEquals(1, tradeHistory.size)

        }
    }

    @Test
    fun testTradeHistoryWithNoMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.get("/orders/trade-history") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }
    }

    @Test
    fun testOrderBookAddBidOrder() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val orderBook = Json.decodeFromString<OrderBook>(bodyAsText())
            assertEquals(1, orderBook.Bids.size)
            assertEquals(0, orderBook.Asks.size)
        }
    }

    @Test
    fun testOrderBookAddAskOrder() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val orderBook = Json.decodeFromString<OrderBook>(bodyAsText())
            assertEquals(0, orderBook.Bids.size)
            assertEquals(1, orderBook.Asks.size)
        }
    }

    @Test
    fun testOrderBookAddMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "2",
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
        }

        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val orderBook = Json.decodeFromString<OrderBook>(bodyAsText())
            assertEquals(0, orderBook.Bids.size)
            assertEquals(0, orderBook.Asks.size)
        }
    }

    @Test
    fun testOrderBookNoMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val orderBook = Json.decodeFromString<OrderBook>(bodyAsText())
            assertEquals(1, orderBook.Bids.size)
            assertEquals(0, orderBook.Asks.size)
        }
    }

    @Test
    fun testPartialOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "1",
                "pair": "BTCUSD",
                "postOnly": true,
                "price": "10000",
                "quantity": "2",
                "side": "SELL",
                "timeInForce": "GTC"
            }
            """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "2",
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
        }

        client.get("/orders/order-book") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val orderBook = Json.decodeFromString<OrderBook>(bodyAsText())
            assert(orderBook.Bids.isEmpty())
            assert(orderBook.Asks.isNotEmpty())
            assertEquals(1, orderBook.Asks.size)
        }
    }

    @Test
    fun testOpenOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.get("/orders/open") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }
    }

    @Test
    fun testOpenOrdersWithMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "1",
                "pair": "BTCUSD",
                "postOnly": true,
                "price": "10000",
                "quantity": "2",
                "side": "SELL",
                "timeInForce": "GTC"
            }
            """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "2",
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
        }

        client.get("/orders/open") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val openOrders = Json.decodeFromString<List<JsonObject>>(bodyAsText())
            assert(openOrders.isNotEmpty())
            assertEquals(1, openOrders.size)
        }
    }

    @Test
    fun testOpenOrdersWithNoMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
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
        }

        client.get("/orders/open") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val openOrders = Json.decodeFromString<List<JsonObject>>(bodyAsText())
            assert(openOrders.isNotEmpty())
            assertEquals(1, openOrders.size)
        }
    }

    @Test
    fun testGetOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.get("/orders/all") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("[]", bodyAsText())
        }
    }

    @Test
    fun testGetOrdersWithMatchingOrders() = testApplication {
        val token = loginAndGetToken(client)

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "1",
                "pair": "BTCUSD",
                "postOnly": true,
                "price": "10000",
                "quantity": "2",
                "side": "SELL",
                "timeInForce": "GTC"
            }
            """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.post("/orders/limit") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                """
            {
                "allowMargin": "true",
                "customerOrderId": "2",
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
        }

        client.get("/orders/all") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val orders = Json.decodeFromString<List<JsonObject>>(bodyAsText())
            assert(orders.isNotEmpty())
            assertEquals(3, orders.size)
        }
    }
}
