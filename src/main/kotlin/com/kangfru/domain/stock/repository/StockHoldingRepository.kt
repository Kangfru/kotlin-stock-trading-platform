package com.kangfru.domain.stock.repository

import com.kangfru.domain.stock.model.StockHolding
import org.springframework.data.jpa.repository.JpaRepository

interface StockHoldingRepository : JpaRepository<StockHolding, Long> {
    fun findByAccountIdAndStockCode(accountId: Long, stockCode: String): StockHolding?

    fun findByAccountId(accountId: Long): List<StockHolding>
}