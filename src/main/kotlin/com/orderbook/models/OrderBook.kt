package com.orderbook.models
import com.orderbook.dto.OrderBookDto
import kotlinx.serialization.Serializable

@Serializable
data class OrderBook(
    val Asks: List<Ask>,
    val Bids: List<Bid>,
    val LastChange: String,
    val SequenceNumber: Long
)
fun OrderBook.toOrderBookDto() = OrderBookDto(Asks.map { it.toAskDto() }, Bids.map { it.toBidDto() })