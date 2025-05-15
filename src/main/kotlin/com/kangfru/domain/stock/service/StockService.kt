package com.kangfru.domain.stock.service

import com.kangfru.domain.stock.dto.StockHoldingResponse
import com.kangfru.domain.stock.dto.StockResponse
import com.kangfru.domain.stock.model.Stock
import com.kangfru.domain.stock.model.StockHolding
import com.kangfru.domain.stock.repository.StockHoldingRepository
import com.kangfru.domain.stock.repository.StockRepository

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class StockService(
    private val stockRepository: StockRepository,
    private val stockHoldingRepository: StockHoldingRepository
) {

    @Transactional(readOnly = true)
    fun getStock(stockCode: String): StockResponse {
        return stockRepository.findByCode(stockCode)
            ?.toResponse()
            ?: throw IllegalArgumentException("주식을 찾을 수 없습니다: $stockCode")
    }

    @Transactional(readOnly = true)
    fun getStockHoldings(accountId: Long): List<StockHoldingResponse> {
        return stockHoldingRepository.findByAccountId(accountId)
            .map { it.toResponse() }
    }

    @Transactional
    fun updateStockPrice(stockCode: String, newPrice: BigDecimal): StockResponse {
        val stock = stockRepository.findByCode(stockCode)
            ?: throw IllegalArgumentException("주식을 찾을 수 없습니다: $stockCode")

        stock.currentPrice = newPrice
        return stockRepository.save(stock).toResponse()
    }

    @Transactional
    fun createStock(code: String, name: String, initialPrice: BigDecimal): StockResponse {
        if (stockRepository.findByCode(code) != null) {
            throw IllegalArgumentException("이미 존재하는 주식 코드입니다: $code")
        }

        val stock = Stock(
            code = code,
            name = name,
            currentPrice = initialPrice
        )

        return stockRepository.save(stock).toResponse()
    }


    private fun Stock.toResponse() = StockResponse(
        code = code,
        name = name,
        currentPrice = currentPrice
    )

    private fun StockHolding.toResponse() = StockHoldingResponse(
        accountId = accountId,
        stockCode = stockCode,
        quantity = quantity,
        averagePrice = averagePrice
    )


}