package com.orderbook

import com.orderbook.models.LimitOrder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MatchingEngineTest {
    @Test
    fun testAddBidOrderNoMatch()= runTest {
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

        //assert that there are no trades
        assert(orderBookService.trades.isEmpty())
    }

    @Test
    fun testAddAskOrderNoMatch()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())
        assert(orderBook.Asks.isNotEmpty())

        //assert that there are no trades
        assert(orderBookService.trades.isEmpty())
    }

    @Test
    fun testAddBidOrderMatch()= runTest {
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
        orderBookService.addLimitOrder(limitOrder1)
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
        orderBookService.addLimitOrder(limitOrder2)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())
        assert(orderBook.Asks.isEmpty())

        //assert that there is a trade
        assert(orderBookService.trades.isNotEmpty())
    }

    @Test
    fun testAddAskOrderMatch()= runTest {
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
        orderBookService.addLimitOrder(limitOrder1)
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
        orderBookService.addLimitOrder(limitOrder2)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())
        assert(orderBook.Asks.isEmpty())

        //assert that there is a trade
        assert(orderBookService.trades.isNotEmpty())
    }

    @Test
    fun testAddBidOrderMatchQuantity()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "2",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
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
        orderBookService.addLimitOrder(limitOrder2)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())
        assert(orderBook.Asks.isNotEmpty())

        //assert that there is a trade
        assert(orderBookService.trades.isNotEmpty())
        //assert that the trade quantity is 1
        assert(orderBookService.trades.first().quantity.toDouble() == 1.0)
    }

    @Test
    fun testAddBidOrderMatchQuantityMultipleAsks()= runTest {
        val orderBookService = OrderBookService()
        val limitOrder1 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "1",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "2",
            side = "SELL",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder1)
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
        orderBookService.addLimitOrder(limitOrder2)
        val limitOrder3 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "3",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder3)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isEmpty())

        //assert that there are two trades
        assert(orderBookService.trades.size == 2)
        //assert that the first trade quantity is 1
        assert(orderBookService.trades.first().quantity.toDouble() == 1.0)
        //assert that the second trade quantity is 1
        assert(orderBookService.trades[1].quantity.toDouble() == 1.0)
    }

    @Test
    fun testAddBidOrderMatchQuantityMultipleBids()= runTest {
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
        orderBookService.addLimitOrder(limitOrder1)
        val limitOrder2 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "2",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "2",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder2)
        val limitOrder3 = LimitOrder(
            allowMargin = "true",
            customerOrderId = "3",
            pair = "BTCUSD",
            postOnly = true,
            price = "10000",
            quantity = "1",
            side = "BUY",
            timeInForce = "GTC"
        )
        orderBookService.addLimitOrder(limitOrder3)
        val orderBook = orderBookService.getOrderBook()
        assert(orderBook.Bids.isNotEmpty())
        assert(orderBook.Asks.isEmpty())
        //assert that there's 1 trade
        assert(orderBookService.trades.size == 1)
        //assert that the first trade quantity is 1
        assert(orderBookService.trades.first().quantity.toDouble() == 1.0)
    }
}