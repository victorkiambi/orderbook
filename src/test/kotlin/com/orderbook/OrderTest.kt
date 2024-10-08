package com.orderbook

import com.orderbook.models.LimitOrder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OrderTest {

    @Test
    fun testAddOrder()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder)
        val orderBook = orderBookService.getOrders()
        assert(orderBook.isNotEmpty())
    }

    @Test
    fun testOpenOrders()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder)
        val openOrders = orderBookService.getOpenOrders()
        assert(openOrders.isNotEmpty())
    }

    @Test
    fun testMatchOrders()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        val limitOrder2 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
        orderBookService.addLimitOrder(limitOrder2)
        val orders = orderBookService.getOrders()
        assert(orders.isNotEmpty())
        assert(orders.size == 2)

        val openOrders = orderBookService.getOpenOrders()
        assert(openOrders.isEmpty())
    }

    @Test
    fun testPartialMatchOrders()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "2",
            side = "BUY",
            timeInForce = "GTC"
        )
        val limitOrder2 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
        orderBookService.addLimitOrder(limitOrder2)
        val orders = orderBookService.getOrders()
        assert(orders.isNotEmpty())
        assert(orders.size == 2)

        val openOrders = orderBookService.getOpenOrders()
        assert(openOrders.isNotEmpty())
        assert(openOrders.size == 1)
    }

    @Test
    fun testNoMatchOrders() = runTest{
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        val limitOrder2 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
        orderBookService.addLimitOrder(limitOrder2)
        val orders = orderBookService.getOrders()
        assert(orders.isNotEmpty())
        assert(orders.size == 2)

        val openOrders = orderBookService.getOpenOrders()
        assert(openOrders.isNotEmpty())
        assert(openOrders.size == 2)
    }
}