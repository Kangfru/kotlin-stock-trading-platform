package com.kangfru.common.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.util.*


@SpringBootTest
class RedisConfigTest {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    private val logger = mu.KotlinLogging.logger {}

    @Test
    fun `Redis 연결 테스트`() {
        try {
            val pingResult = redisTemplate.execute { connection ->
                connection.ping()
            }
            logger.info { "Redis 연결 성공: $pingResult" }

            // 간단한 setIfAbsent 테스트
            val testKey = "test:connection:${UUID.randomUUID()}"
            val testValue = "test-value"
            val result = redisTemplate.opsForValue().setIfAbsent(testKey, testValue, Duration.ofSeconds(10))

            logger.info { "setIfAbsent 테스트 결과: $result" }

            // 정리
            redisTemplate.delete(testKey)
        } catch (e: Exception) {
            logger.error { "Redis 연결 테스트 실패: ${e.message}" }
            throw e
        }
    }
}
