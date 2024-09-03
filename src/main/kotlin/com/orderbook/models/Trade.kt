package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class Trade(
    val currencyPair: String,
    val id: String,
    val price: String,
    val quantity: String,
    val quoteVolume: String,
    val sequenceId: Long,
    val takerSide: String,
    val tradedAt: String
)