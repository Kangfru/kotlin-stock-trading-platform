package com.kangfru.domain.stock.controller

import com.kangfru.domain.stock.dto.CreateStockRequest
import com.kangfru.domain.stock.dto.StockHoldingResponse
import com.kangfru.domain.stock.dto.StockResponse
import com.kangfru.domain.stock.dto.UpdateStockPriceRequest
import com.kangfru.domain.stock.service.StockService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stocks")
class StockController(
    private val stockService: StockService
) {

    @GetMapping("/{stockCode}")
    fun getStock(@PathVariable stockCode: String): ResponseEntity<StockResponse> {
        val response = stockService.getStock(stockCode)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/holdings")
    fun getStockHoldings(
        @RequestParam accountId: Long
    ): ResponseEntity<List<StockHoldingResponse>> {
        val response = stockService.getStockHoldings(accountId)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createStock(
        @RequestBody @Valid request: CreateStockRequest
    ): ResponseEntity<StockResponse> {
        val response = stockService.createStock(
            code = request.code,
            name = request.name,
            initialPrice = request.initialPrice
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{stockCode}/price")
    fun updateStockPrice(
        @PathVariable stockCode: String,
        @RequestBody @Valid request: UpdateStockPriceRequest
    ): ResponseEntity<StockResponse> {
        val response = stockService.updateStockPrice(stockCode, request.newPrice)
        return ResponseEntity.ok(response)
    }

}