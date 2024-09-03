package com.orderbook.models

import com.orderbook.dto.AskDto
import kotlinx.serialization.Serializable

@Serializable
data class Ask(
    val currencyPair: String,
    val orderCount: Int,
    val price: String,
    val quantity: String,
    val side: String
)
fun Ask.toAskDto() = AskDto(price, quantity)