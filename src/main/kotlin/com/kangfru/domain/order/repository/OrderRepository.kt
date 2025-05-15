package com.kangfru.domain.order.repository

import com.kangfru.domain.order.model.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {

    fun findByAccountId(accountId: Long): List<Order>

}