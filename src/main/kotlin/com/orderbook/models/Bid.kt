package com.orderbook.models
import com.orderbook.dto.BidDto
import kotlinx.serialization.Serializable

@Serializable
data class Bid(
    val currencyPair: String,
    val orderCount: Int,
    val price: String,
    val quantity: String,
    val side: String
)
fun Bid.toBidDto() = BidDto(price, quantity)