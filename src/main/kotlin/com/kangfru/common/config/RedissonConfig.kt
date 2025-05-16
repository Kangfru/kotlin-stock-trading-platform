package com.kangfru.common.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig {

    @Bean
    fun redissonClient(redisProperties: RedisProperties): RedissonClient {
        val config = Config()

        // 단일 노드 설정
        val singleServerConfig = config.useSingleServer()
        singleServerConfig.address = "redis://${redisProperties.host}:${redisProperties.port}"

        // 비밀번호가 있는 경우
        if (!redisProperties.password.isNullOrEmpty()) {
            singleServerConfig.password = redisProperties.password
        }

        // 연결 풀 설정
        singleServerConfig.connectionMinimumIdleSize = 5
        singleServerConfig.connectionPoolSize = 20
        singleServerConfig.connectTimeout = 5000

        // 스레드 설정
        config.threads = 4
        config.nettyThreads = 8

        return Redisson.create(config)
    }
}