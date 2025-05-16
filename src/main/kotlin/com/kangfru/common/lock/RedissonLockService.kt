package com.kangfru.common.lock

import mu.KotlinLogging
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedissonLockService(private val redissonClient: RedissonClient) {
    private val logger = KotlinLogging.logger {}

    fun <T> executeWithLock(
        lockKey: String,
        waitTimeMs: Long = 30000L,
        leaseTimeMs: Long = 10000L,
        action: () -> T
    ): T {
        val lock = redissonClient.getLock(lockKey)
        val threadId = Thread.currentThread().id

        logger.debug { "[$threadId] 락 획득 시도: $lockKey (최대 대기: ${waitTimeMs}ms)" }
        val startTime = System.currentTimeMillis()

        // 락 획득 시도
        val locked = if (leaseTimeMs > 0) {
            // 지정된 시간 동안 락 유지 (자동 해제)
            lock.tryLock(waitTimeMs, leaseTimeMs, TimeUnit.MILLISECONDS)
        } else {
            // 수동으로 해제할 때까지 락 유지
            lock.tryLock(waitTimeMs, TimeUnit.MILLISECONDS)
        }

        if (!locked) {
            val elapsed = System.currentTimeMillis() - startTime
            logger.error { "[$threadId] 락 획득 실패: $lockKey (${elapsed}ms 경과)" }
            throw LockAcquisitionTimeoutException("락 획득 시간 초과: $lockKey")
        }

        val acquireTime = System.currentTimeMillis() - startTime
        logger.debug { "[$threadId] 락 획득 성공: $lockKey (${acquireTime}ms 소요)" }

        try {
            // 락 획득 성공, 작업 수행
            return action()
        } finally {
            // 락이 여전히 이 스레드에 의해 보유되고 있는지 확인
            if (lock.isHeldByCurrentThread) {
                try {
                    lock.unlock()
                    logger.debug { "[$threadId] 락 해제 완료: $lockKey" }
                } catch (e: IllegalMonitorStateException) {
                    logger.warn { "[$threadId] 락 해제 실패 (이미 해제됨): $lockKey" }
                }
            } else {
                logger.warn { "[$threadId] 락 해제 스킵 (소유하지 않음): $lockKey" }
            }
        }
    }

fun getLockInfo(lockKey: String): LockInfo {
        val lock = redissonClient.getLock(lockKey)

        return try {
            LockInfo(
                name = lockKey,
                locked = lock.isLocked,
                heldByCurrentThread = lock.isHeldByCurrentThread,
                remainTimeToLive = if (lock.isLocked) lock.remainTimeToLive() else 0
            )
        } catch (e: Exception) {
            logger.error { "락 정보 조회 중 오류: $lockKey - ${e.message}" }
            LockInfo(name = lockKey, locked = false, error = e.message)
        }
    }

fun forceUnlock(lockKey: String): Boolean {
        val lock = redissonClient.getLock(lockKey)

        return try {
            lock.forceUnlock()
            logger.warn { "락 강제 해제: $lockKey" }
            true
        } catch (e: Exception) {
            logger.error { "락 강제 해제 중 오류: $lockKey - ${e.message}" }
            false
        }
    }

    data class LockInfo(
        val name: String,
        val locked: Boolean = false,
        val heldByCurrentThread: Boolean = false,
        val remainTimeToLive: Long = 0,
        val error: String? = null
    )

}

class LockAcquisitionTimeoutException(message: String) : RuntimeException(message)
