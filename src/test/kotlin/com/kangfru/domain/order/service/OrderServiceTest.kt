package com.kangfru.domain.order.service

import com.kangfru.domain.account.model.Account
import com.kangfru.domain.account.repository.AccountRepository
import com.kangfru.domain.order.dto.CreateOrderRequest
import com.kangfru.domain.order.model.OrderType
import com.kangfru.domain.stock.model.Stock
import com.kangfru.domain.stock.repository.StockHoldingRepository
import com.kangfru.domain.stock.repository.StockRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var stockRepository: StockRepository

    @Autowired
    private lateinit var stockHoldingRepository: StockHoldingRepository

    private lateinit var testAccount: Account
    private lateinit var testStock: Stock

    @BeforeEach
    fun setup() {
        // 테스트용 계좌 생성 (잔고: 100,000원)
        testAccount = accountRepository.save(
            Account(
                balance = BigDecimal("100000"),
                userId = 1L
            )
        )

        // 테스트용 주식 생성 (가격: 1,000원)
        testStock = stockRepository.save(
            Stock(
                code = "TEST001",
                name = "테스트주식",
                currentPrice = BigDecimal("1000")
            )
        )
    }

    @AfterEach
    fun clear() {
        accountRepository.delete(testAccount)
        stockRepository.delete(testStock)
    }

    @Test
    fun `잔고가 부족한 상황에서 동시 주문 시 분산락이 정상 동작하는지 테스트`() {
        // given
        val threadCount = 5
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        val orderQuantity = 20L // 각 주문당 20주씩
        val orderPrice = BigDecimal("1000") // 주당 1,000원
        // 총 필요 금액: 20,000 * 5 = 100,000원 (정확히 잔고만큼)

        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        repeat(threadCount) {
            executor.submit {
                try {
                    orderService.createOrder(
                        CreateOrderRequest(
                            accountId = testAccount.id!!,
                            stockCode = testStock.code,
                            orderType = OrderType.BUY,
                            quantity = orderQuantity,
                            price = orderPrice
                        )
                    )
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        // then
        val updatedAccount = accountRepository.findByIdOrNull(testAccount.id!!)!!
        val finalStockHolding = stockHoldingRepository
            .findByAccountIdAndStockCode(testAccount.id!!, testStock.code)

        assertTrue(
            successCount.get() + failCount.get() == threadCount,
            "모든 요청이 성공 또는 실패로 처리되어야 합니다"
        )

        assertTrue(
            successCount.get() <= 5,
            "잔고를 초과하는 주문은 실패해야 합니다"
        )

        assertTrue(
            updatedAccount.balance >= BigDecimal.ZERO,
            "잔고는 마이너스가 되면 안됩니다"
        )

        val expectedQuantity = successCount.get() * orderQuantity
        assertEquals(
            expectedQuantity,
            finalStockHolding?.quantity ?: 0,
            "성공한 주문 수량만큼만 보유해야 합니다"
        )
    }
}
