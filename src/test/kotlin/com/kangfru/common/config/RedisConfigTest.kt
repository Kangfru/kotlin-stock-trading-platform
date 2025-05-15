package com.kangfru.common.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate


@SpringBootTest
class RedisConfigTest {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Test
    fun `Redis 연결 테스트`() {
        // given
        val key = "test:key"
        val value = "test:value"

        // when
        redisTemplate.opsForValue().set(key, value)
        val result = redisTemplate.opsForValue().get(key)

        // then
        assertEquals(value, result)

        // cleanup
        redisTemplate.delete(key)
    }
}
