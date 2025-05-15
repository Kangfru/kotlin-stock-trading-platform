package com.kangfru.domain.order.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.LocalDateTime

enum class OrderType {
    BUY, SELL
}

enum class OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    FAILED
}

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val accountId: Long,

    @Column(nullable = false)
    val stockCode: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val orderType: OrderType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    @Column(nullable = false)
    val quantity: Long,

    @Column(nullable = false, precision = 19, scale = 4)
    val price: BigDecimal,

    @Version
    var version: Long = 0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null

) {
    fun complete() {
        require(status == OrderStatus.PENDING) { "처리할 수 없는 주문 상태 입니다." }
        status = OrderStatus.COMPLETED
        updatedAt = LocalDateTime.now()
    }

    fun cancel() {
        require(status == OrderStatus.PENDING) { "취소할 수 없는 주문 상태 입니다." }
        status = OrderStatus.CANCELLED
        updatedAt = LocalDateTime.now()
    }

    fun fail() {
        require(status == OrderStatus.PENDING) { "실패 처리할 수 없는 주문 상태 입니다." }
        status = OrderStatus.FAILED
        updatedAt = LocalDateTime.now()
    }

    fun getTotalAmount(): BigDecimal = price.multiply(BigDecimal(quantity))
}