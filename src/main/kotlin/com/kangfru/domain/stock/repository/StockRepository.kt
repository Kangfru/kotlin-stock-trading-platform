package com.kangfru.domain.stock.repository

import com.kangfru.domain.stock.model.Stock
import org.springframework.data.jpa.repository.JpaRepository

interface StockRepository : JpaRepository<Stock, Long> {
    fun findByCode(code: String): Stock?
}