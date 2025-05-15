package com.kangfru

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinStockTradingPlatformApplication

fun main(args: Array<String>) {
    runApplication<KotlinStockTradingPlatformApplication>(*args)
}
