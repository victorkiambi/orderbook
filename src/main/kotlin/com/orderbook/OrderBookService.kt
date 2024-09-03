package com.orderbook

import com.orderbook.models.Order
import com.orderbook.models.OrderBook
import com.orderbook.models.Trade
import org.slf4j.LoggerFactory
import java.util.*

class OrderBookService {
    private val asks = PriorityQueue<Order>(compareBy { it.price.toDouble() })
    private val bids = PriorityQueue<Order>(compareByDescending { it.price.toDouble() })
    var trades = mutableListOf<Trade>()
    private val logger = LoggerFactory.getLogger(OrderBookService::class.java)


    fun getOrderBook(): OrderBook {
        return OrderBook(
            asks.toMutableList(), bids.toMutableList(),
            LastChange = "",
            SequenceNumber = "1".toLong()
        )
    }

    fun addOrder(order: Order): Trade? {
        if (order.side.uppercase() == "BUY") {
            bids.offer(order)

        } else if (order.side.uppercase() == "SELL") {
            asks.offer(order)
        }
        return matchOrders()
    }

    private fun matchOrders(): Trade? {
        if (bids.isEmpty() || asks.isEmpty()) {
            logger.info("No bids or asks")
            return null
        }

        val bid = bids.peek()
        val ask = asks.peek()

        // Early return if no match
        if (bid.price.toDouble() < ask.price.toDouble()) {
            logger.info("No match")
            return null
        }

        val tradeQuantity = minOf(bid.quantity, ask.quantity)
        val trade = Trade(
            bid.customerOrderId,
            ask.customerOrderId,
            bid.price,
            tradeQuantity,
            bid.pair,
            tradedAt = System.currentTimeMillis().toString(),
            takerSide = bid.side,
            sequenceId = 1,
        )

        trades.add(trade)
        logger.info("Trade created: $trade")

        bid.quantity = (bid.quantity.toDouble() - tradeQuantity.toDouble()).toString()
        ask.quantity = (ask.quantity.toDouble() - tradeQuantity.toDouble()).toString()

        // Remove fully fulfilled orders
        if (bid.quantity.toDouble() == 0.0) bids.poll()
        if (ask.quantity.toDouble() == 0.0) asks.poll()

        return trade
    }
    
    //get trade history
    fun getTradeHistory(): List<Trade>{
        logger.info("Trade history: $trades")
        return trades
    }
}