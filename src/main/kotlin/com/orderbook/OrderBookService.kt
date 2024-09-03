package com.orderbook

import com.orderbook.models.Order
import com.orderbook.models.OrderBook
import com.orderbook.models.Trade
import org.slf4j.LoggerFactory

class OrderBookService {
    private val asks = mutableListOf<Order>()
    private val bids = mutableListOf<Order>()
    var trades = mutableListOf<Trade>()
    private val logger = LoggerFactory.getLogger(OrderBookService::class.java)


    fun getOrderBook(): OrderBook {
        return OrderBook(
            asks, bids,
            LastChange = "",
            SequenceNumber = "1".toLong()
        )
    }

    fun addOrder(order: Order): Trade? {
        if (order.side.uppercase() == "BUY") {
            bids.add(order)
            bids.sortByDescending { it.price.toDouble() }

        } else if (order.side.uppercase() == "SELL") {
            asks.add(order)
            asks.sortBy { it.price.toDouble() }
        }
        return matchOrders()
    }

    //match orders
    private fun matchOrders(): Trade? {
        //if there are no bids or asks, return
        if(bids.isEmpty() || asks.isEmpty()){
            logger.info("No bids or asks")
            return null
        }

        //if the highest bid is less than the lowest ask, return
        if(bids.first().price.toDouble() < asks.first().price.toDouble()){
            logger.info("No match")
            return null
        }

        //if the highest bid is greater than the lowest ask, match the orders
        val bid = bids.first()
        val ask = asks.first()

        logger.info("Matched bid: $bid with ask: $ask")
        //if the bid quantity is greater than the ask quantity, create a trade
        if(bid.quantity.toDouble() > ask.quantity.toDouble()){
            logger.info("Bid quantity is greater than ask quantity")
            val trade = Trade(
                bid.customerOrderId,
                ask.customerOrderId,
                bid.price,
                ask.quantity,
                bid.pair,
                tradedAt = System.currentTimeMillis().toString(),
                takerSide = bid.side,
                sequenceId = 1,
            )
            trades.add(trade)
            bid.quantity = (bid.quantity.toDouble() - ask.quantity.toDouble()).toString()
            asks.removeAt(0)

            logger.info("Trade created: $trade")
            return trade
        }

        //if the bid quantity is less than the ask quantity, create a trade
        else if(bid.quantity.toDouble() < ask.quantity.toDouble()){
            logger.info("Bid quantity is less than ask quantity")
            val trade = Trade(
                bid.customerOrderId,
                ask.customerOrderId,
                bid.price,
                bid.quantity,
                bid.pair,
                tradedAt = System.currentTimeMillis().toString(),
                takerSide = bid.side,
                sequenceId = 1,
            )
            trades.add(trade)
            ask.quantity = (ask.quantity.toDouble() - bid.quantity.toDouble()).toString()
            bids.removeAt(0)

            logger.info("Trade created: $trade")
            return trade
        }

        //if the bid quantity is equal to the ask quantity, create a trade
        else{
            logger.info("Bid quantity is equal to ask quantity")
            val trade = Trade(
                bid.customerOrderId,
                ask.customerOrderId,
                bid.price,
                bid.quantity,
                bid.pair,
                tradedAt = System.currentTimeMillis().toString(),
                takerSide = bid.side,
                sequenceId = 1,
            )
            trades.add(trade)
            bids.removeAt(0)
            asks.removeAt(0)

            logger.info("Trade created: $trade")
            return trade
        }
    }

    //get trade history
    fun getTradeHistory(): List<Trade>{
        logger.info("Trade history: $trades")
        return trades
    }
}