package com.kangfru.domain.order.service

import com.kangfru.domain.account.model.Account
import com.kangfru.domain.account.repository.AccountRepository
import com.kangfru.domain.order.dto.CreateOrderRequest
import com.kangfru.domain.order.model.OrderType
import com.kangfru.domain.stock.model.Stock
import com.kangfru.domain.stock.repository.StockHoldingRepository
import com.kangfru.domain.stock.repository.StockRepository
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class OrderServiceTest {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var stockRepository: StockRepository

    @Autowired
    private lateinit var stockHoldingRepository: StockHoldingRepository

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

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
        try {
            // 최신 상태로 다시 조회 후 삭제
            testAccount.id?.let { accountId ->
                val freshAccount = accountRepository.findById(accountId)
                if (freshAccount.isPresent) {
                    accountRepository.delete(freshAccount.get())
                }
            }

            testStock.id?.let { stockId ->
                val freshStock = stockRepository.findById(stockId)
                if (freshStock.isPresent) {
                    stockRepository.delete(freshStock.get())
                }
            }

            // 테스트 중 생성된 주식 보유량 데이터도 삭제
            testAccount.id?.let { accountId ->
                stockHoldingRepository.deleteByAccountId(accountId)
            }
        } catch (e: Exception) {
            logger.error(e) { "테스트 정리 중 오류 발생" }
        }
    }


    @Test
    fun `잔고가 부족한 상황에서 동시 주문 시 분산락이 정상 동작하는지 테스트`() {
        // given
        logger.info { "테스트 시작: 잔고가 부족한 상황에서 동시 주문 시 분산락 테스트" }

        val lockKey = "LOCK:ORDER:ACCOUNT:${testAccount.id}"
        if (redisTemplate.hasKey(lockKey)) {
            logger.warn { "테스트 시작 전 이미 락이 존재합니다: $lockKey - 삭제 시도" }
            redisTemplate.delete(lockKey)
        }


        val threadCount = 5
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        val orderQuantity = 21L // 각 주문당 20주씩
        val orderPrice = BigDecimal("1000") // 주당 1,000원
        // 총 필요 금액: 20,000 * 5 = 100,000원 (정확히 잔고만큼)

        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // when
        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    logger.debug { "스레드 $threadIndex: 주문 시작 (accountId: ${testAccount.id}, stockCode: ${testStock.code})" }

                    orderService.createOrder(
                        CreateOrderRequest(
                            accountId = testAccount.id!!,
                            stockCode = testStock.code,
                            orderType = OrderType.BUY,
                            quantity = orderQuantity,
                            price = orderPrice
                        )
                    )
                    logger.debug { "스레드 $threadIndex: 주문 성공" }
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    logger.error(e) { "스레드 $threadIndex: 주문 실패" }
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        logger.info { "테스트 완료 : 성공=$successCount, 실패=$failCount" }

        // then
        val updatedAccount = accountRepository.findByIdOrNull(testAccount.id!!)!!
        logger.info { "최종 계좌 잔액: ${updatedAccount.balance}" }
        val finalStockHolding = stockHoldingRepository
            .findByAccountIdAndStockCode(testAccount.id!!, testStock.code)
        logger.info { "최종 주식 보유량: ${finalStockHolding?.quantity ?: 0}" }

        assertTrue(
            successCount.get() + failCount.get() == threadCount,
            "모든 요청이 성공 또는 실패로 처리되어야 합니다"
        )

        assertTrue(
            successCount.get() <= 4,
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
