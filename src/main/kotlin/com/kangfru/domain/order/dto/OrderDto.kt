package com.kangfru.domain.order.dto

import com.kangfru.domain.order.model.OrderStatus
import com.kangfru.domain.order.model.OrderType
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreateOrderRequest(
    @field:NotNull(message = "계좌 ID는 필수입니다")
    @field:Positive(message = "계좌 ID는 양수여야 합니다")
    val accountId: Long,

    @field:NotBlank(message = "주식 코드는 필수입니다")
    @field:Pattern(
        regexp = "^[A-Z0-9]{6}$",
        message = "주식 코드는 6자리 영문대문자와 숫자의 조합이어야 합니다"
    )
    val stockCode: String,

    @field:NotNull(message = "주문 유형은 필수입니다")
    val orderType: OrderType,

    @field:NotNull(message = "주문 수량은 필수입니다")
    @field:Min(value = 1, message = "주문 수량은 1 이상이어야 합니다")
    val quantity: Long,

    @field:NotNull(message = "주문 가격은 필수입니다")
    @field:Positive(message = "주문 가격은 0보다 커야 합니다")
    @field:Digits(
        integer = 10,
        fraction = 2,
        message = "주문 가격은 최대 10자리 정수와 2자리 소수까지 허용됩니다"
    )
    val price: BigDecimal

)

data class OrderResponse(
    val id: Long,
    val accountId: Long,
    val stockCode: String,
    val orderType: OrderType,
    val status: OrderStatus,
    val quantity: Long,
    val price: BigDecimal,
    val totalAmount: BigDecimal
)