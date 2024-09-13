package com.orderbook.models
import kotlinx.serialization.Serializable

@Serializable
data class OrderBook(
    val Asks: MutableList<Ask>,
    val Bids: MutableList<Bid>,
    val LastChange: String,
    val SequenceNumber: Long)