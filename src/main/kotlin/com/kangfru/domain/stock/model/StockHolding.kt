package com.kangfru.domain.stock.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "stock_holdings")
class StockHolding(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val accountId: Long,

    @Column(nullable = false)
    val stockCode: String,

    @Column(nullable = false)
    var quantity: Long,

    @Column(nullable = false, precision = 19, scale = 4)
    var averagePrice: BigDecimal,

    @Version
    var version: Long = 0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null
) {

    fun addQuantity(amount: Long, price: BigDecimal) {
        val newTotalQuantity = quantity + amount
        val newTotalValue = averagePrice.multiply(BigDecimal(quantity))
            .add(price.multiply(BigDecimal(amount)))

        quantity = newTotalQuantity
        averagePrice = newTotalValue.divide(BigDecimal(newTotalQuantity))
        updatedAt = LocalDateTime.now()
    }

    fun removeQuantity(amount: Long) {
        require(quantity >= amount) { "보유 수량이 부족합니다." }
        quantity -= amount
        updatedAt = LocalDateTime.now()
    }

}