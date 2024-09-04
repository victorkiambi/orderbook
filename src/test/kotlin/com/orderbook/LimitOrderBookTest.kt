package com.orderbook

import com.orderbook.models.LimitOrder
import kotlin.test.Test


class LimitOrderBookTest {
    @Test
    fun testAddOrder() {
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
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
    }

    @Test
    fun testGetOrderBook() {
        val orderBookService = OrderBookService()
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Asks.isEmpty())
        assert(orderBook.Bids.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckOrderBook() {
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
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
        assert(orderBook.Asks.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckTradeHistory() {
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
        val tradeHistory = orderBookService.getTradeHistory()
        assert(tradeHistory.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckTradeHistoryWithMatch() {
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
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
        val tradeHistory = orderBookService.getTradeHistory()
        assert(tradeHistory.isNotEmpty())
    }

    @Test
    fun testAddOrderAndCheckTradeHistoryWithNoMatch() {
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
            price = "10001",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
        orderBookService.addLimitOrder(limitOrder2)
        val tradeHistory = orderBookService.getTradeHistory()
        assert(tradeHistory.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckOrderBookWithMatch() {
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
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
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())
        assert(orderBook.Asks.isEmpty())
    }

    @Test
    fun testAddOrderAndCheckOrderBookWithNoMatch() {
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
            price = "10001",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
        orderBookService.addLimitOrder(limitOrder2)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
        assert(orderBook.Asks.isNotEmpty())
    }

    @Test
    fun testAddOrderAndCheckOpenOrders() {
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
}