package com.kangfru.common.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.hibernate.exception.LockAcquisitionException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
class DistributedLockAspect(
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Around("@annotation(com.kangfru.common.lock.DistributedLock)")
    fun executeWithLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val lockAnnotation = method.getAnnotation(DistributedLock::class.java)

        val expressionParser = SpelExpressionParser()
        val context = StandardEvaluationContext()

        // 메서드 파라미터 이름과 값을 컨텍스트에 추가
        val parameterNames = signature.parameterNames
        val args = joinPoint.args
        for (i in parameterNames.indices) {
            context.setVariable(parameterNames[i], args[i])
        }

        // SpEL을 사용하여 키 생성
        val expression = expressionParser.parseExpression(lockAnnotation.key)
        val lockKey = "LOCK:" + expression.getValue(context, String::class.java)

        try {
            val acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", lockAnnotation.leaseTime, TimeUnit.MILLISECONDS)

            if (acquired != true) {
                throw LockAcquisitionException("락 획득 실패: $lockKey")
            }

            return joinPoint.proceed()
        } finally {
            redisTemplate.delete(lockKey)
        }
    }

    private fun generateLockKey(keyPattern: String, args: Array<Any>): String {
        var key = keyPattern
        args.forEachIndexed { index, arg -> key = key.replace("{$index}", arg.toString()) }

        return "LOCK:$key"
    }

}
class LockAcquisitionException(message: String) : RuntimeException(message)
