package com.orderbook.models

import kotlinx.serialization.Serializable

@Serializable
data class Ask(
    val currencyPair: String,
    val orderCount: Int,
    val price: String,
    var quantity: String,
    val side: String
)
{
    fun toAsk(): LimitOrder {
        return LimitOrder(
            "false",
            "0",
            currencyPair,
            false,
            price,
            quantity,
            side,
            "GTC"
        )
    }
}