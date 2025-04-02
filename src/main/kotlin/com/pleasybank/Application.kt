package com.pleasybank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * PleasyBank 애플리케이션 엔트리 포인트
 * 최신 패키지 구조를 스캔하도록 설정
 */
@SpringBootApplication
@ComponentScan(basePackages = [
    "com.pleasybank.core",
    "com.pleasybank.domain",
    "com.pleasybank.integration"
])
@EnableScheduling
class PleasyBankApplication

/**
 * 애플리케이션 시작점
 */
fun main(args: Array<String>) {
    runApplication<PleasyBankApplication>(*args)
} 