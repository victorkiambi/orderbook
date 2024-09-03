package com.orderbook.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderBookDto(
    val asks: List<AskDto>,
    val bids: List<BidDto>,
)
