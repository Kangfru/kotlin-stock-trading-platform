package com.kangfru.domain.stock.repository

import com.kangfru.domain.stock.model.StockHolding
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface StockHoldingRepository : JpaRepository<StockHolding, Long> {
    fun findByAccountIdAndStockCode(accountId: Long, stockCode: String): StockHolding?

    fun findByAccountId(accountId: Long): List<StockHolding>

    @Modifying
    @Transactional
    fun deleteByAccountId(accountId: Long)

}