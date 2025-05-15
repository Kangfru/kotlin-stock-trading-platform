package com.kangfru.domain.stock.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreateStockRequest(
    @field:NotBlank(message = "주식 코드는 필수입니다")
    val code: String,

    @field:NotBlank(message = "주식 이름은 필수입니다")
    val name: String,

    @field:Positive(message = "초기 가격은 0보다 커야 합니다")
    val initialPrice: BigDecimal
)

data class UpdateStockPriceRequest(
    @field:Positive(message = "가격은 0보다 커야 합니다")
    val newPrice: BigDecimal
)


data class StockResponse(
    val code: String,
    val name: String,
    val currentPrice: BigDecimal
)

data class StockHoldingResponse(
    val accountId: Long,
    val stockCode: String,
    val quantity: Long,
    val averagePrice: BigDecimal
)