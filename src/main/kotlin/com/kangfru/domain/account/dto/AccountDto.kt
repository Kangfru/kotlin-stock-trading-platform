package com.kangfru.domain.account.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateAccountRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    val userId: Long,

    @field:NotNull(message = "초기 잔액은 필수입니다")
    @field:Min(value = 0, message = "초기 잔액은 0 이상이어야 합니다")
    val initialBalance: BigDecimal

)

data class AccountResponse(
    val id: Long,
    val userId: Long,
    val balance: BigDecimal
)