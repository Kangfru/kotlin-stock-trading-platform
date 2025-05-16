package com.kangfru.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private lateinit var port: String

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration().apply {
            hostName = host
            port = port.toInt()
        }
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()

//             트랜잭션 지원 활성화
            setEnableTransactionSupport(true)
            afterPropertiesSet()
        }
    }

    @Bean
    fun lockRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory

        // 트랜잭션 지원 명시적으로 비활성화
        template.setEnableTransactionSupport(false)

        // 직렬화 설정
        val stringSerializer = StringRedisSerializer()
        template.keySerializer = stringSerializer
        template.valueSerializer = stringSerializer
        template.hashKeySerializer = stringSerializer
        template.hashValueSerializer = stringSerializer

        return template
    }

}