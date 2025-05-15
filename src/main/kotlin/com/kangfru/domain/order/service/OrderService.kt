package com.kangfru.domain.order.service

import com.kangfru.common.lock.DistributedLock
import com.kangfru.domain.account.model.Account
import com.kangfru.domain.account.repository.AccountRepository
import com.kangfru.domain.order.dto.CreateOrderRequest
import com.kangfru.domain.order.dto.OrderResponse
import com.kangfru.domain.order.model.Order
import com.kangfru.domain.order.model.OrderStatus
import com.kangfru.domain.order.model.OrderType
import com.kangfru.domain.order.repository.OrderRepository
import com.kangfru.domain.stock.model.StockHolding
import com.kangfru.domain.stock.repository.StockHoldingRepository
import com.kangfru.domain.stock.repository.StockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val accountRepository: AccountRepository,
    private val stockHoldingRepository: StockHoldingRepository,
    private val stockRepository: StockRepository
) {

    @Transactional
    @DistributedLock(key = "'ORDER:ACCOUNT:' + #request.accountId")
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        val account = accountRepository.findByIdWithLock(request.accountId)
            ?: throw IllegalArgumentException("계좌를 찾을 수 없습니다.")

        val stock = stockRepository.findByCode(request.stockCode)
            ?: throw IllegalArgumentException("대상 종목을 찾을 수 없습니다.")

        when (request.orderType) {
            OrderType.BUY -> processBuyOrder(request, account)
            OrderType.SELL -> processSellOrder(request, account)
        }

        val order = Order(
            accountId = request.accountId,
            stockCode = request.stockCode,
            orderType = request.orderType,
            status = OrderStatus.COMPLETED,
            quantity = request.quantity,
            price = request.price
        )

        return orderRepository.save(order).toResponse()
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: Long): OrderResponse {
        return orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("주문 내역을 찾을 수 없습니다: $orderId") }
            .toResponse()
    }

    private fun processBuyOrder(request: CreateOrderRequest, account: Account) {
        val totalAmount = request.price.multiply(BigDecimal(request.quantity))
        account.withdraw(totalAmount)

        val holding = stockHoldingRepository
            .findByAccountIdAndStockCode(request.accountId, request.stockCode)
            ?: StockHolding(
                accountId = request.accountId,
                stockCode = request.stockCode,
                quantity = 0,
                averagePrice = BigDecimal.ZERO
            )

        holding.addQuantity(request.quantity, request.price)
        stockHoldingRepository.save(holding)
    }

    private fun processSellOrder(request: CreateOrderRequest, account: Account) {
        val holding = stockHoldingRepository
            .findByAccountIdAndStockCode(request.accountId, request.stockCode)
            ?: throw IllegalArgumentException("보유한 주식이 없습니다.")

        holding.removeQuantity(request.quantity)

        val totalAmount = request.price.multiply(BigDecimal(request.quantity))
        account.deposit(totalAmount)

        stockHoldingRepository.save(holding)
    }

    private fun Order.toResponse() = OrderResponse(
        id = id!!,
        accountId = accountId,
        stockCode = stockCode,
        orderType = orderType,
        status = status,
        quantity = quantity,
        price = price,
        totalAmount = getTotalAmount()
    )

}