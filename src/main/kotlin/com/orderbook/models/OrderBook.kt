package com.orderbook.models
import kotlinx.serialization.Serializable

@Serializable
data class OrderBook(
    val Asks: MutableList<LimitOrder>,
    val Bids: MutableList<LimitOrder>,
    val LastChange: String,
    val SequenceNumber: Long
)
