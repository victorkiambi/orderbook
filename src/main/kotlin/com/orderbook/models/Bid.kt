package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class Bid(
    val currencyPair: String,
    val orderCount: Int,
    val price: String,
    var quantity: String,
    val side: String
)