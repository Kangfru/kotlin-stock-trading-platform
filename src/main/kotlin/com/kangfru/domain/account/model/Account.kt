package com.kangfru.domain.account.model

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
@Table(name = "accounts")
class Account(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, precision = 19, scale = 4)
    var balance: BigDecimal,

    @Version
    var version: Long = 0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = null
) {
    fun withdraw(amount: BigDecimal) {
        require(balance >= amount) { "잔액이 부족합니다." }
        balance = balance.subtract(amount)
        updatedAt = LocalDateTime.now()
    }

    fun deposit(amount: BigDecimal) {
        balance = balance.add(amount)
        updatedAt = LocalDateTime.now()
    }
}