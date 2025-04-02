package com.pleasybank.core.util

import java.util.Random

/**
 * 은행 및 금융 관련 유틸리티 클래스
 */
object BankUtils {
    /**
     * 은행 코드 맵 (은행 코드와 이름)
     */
    private val BANK_CODE_MAP = mapOf(
        "002" to "KDB산업은행",
        "003" to "IBK기업은행",
        "004" to "KB국민은행",
        "007" to "수협은행",
        "011" to "NH농협은행",
        "020" to "우리은행",
        "023" to "SC제일은행",
        "027" to "한국씨티은행",
        "031" to "대구은행",
        "032" to "부산은행",
        "034" to "광주은행",
        "035" to "제주은행",
        "037" to "전북은행",
        "039" to "경남은행",
        "045" to "새마을금고",
        "048" to "신협",
        "071" to "우체국",
        "081" to "하나은행",
        "088" to "신한은행",
        "089" to "K뱅크",
        "090" to "카카오뱅크",
        "092" to "토스뱅크"
    )
    
    /**
     * 은행 코드로 은행 이름 조회
     */
    fun getBankName(bankCode: String): String {
        return BANK_CODE_MAP[bankCode] ?: "알 수 없는 은행"
    }
    
    /**
     * 은행 코드 유효성 검사
     */
    fun isValidBankCode(bankCode: String): Boolean {
        return BANK_CODE_MAP.containsKey(bankCode)
    }
    
    /**
     * 은행 거래 ID 생성
     */
    fun generateBankTranId(prefix: String = "M202300001U"): String {
        val randomValue = Random().nextInt(999999999).toString().padStart(9, '0')
        return "$prefix$randomValue"
    }
    
    /**
     * 카드 번호 포맷팅 (XXXX-XXXX-XXXX-XXXX)
     */
    fun formatCardNumber(cardNumber: String): String {
        if (cardNumber.length != 16) {
            return cardNumber
        }
        
        return cardNumber.chunked(4).joinToString("-")
    }
    
    /**
     * 계좌번호에서 대시(-) 제거
     */
    fun normalizeBankAccountNumber(accountNumber: String): String {
        return accountNumber.replace("-", "")
    }
    
    /**
     * 계좌번호 포맷팅
     */
    fun formatBankAccountNumber(accountNumber: String, bankCode: String): String {
        val normalized = normalizeBankAccountNumber(accountNumber)
        
        // 은행별 계좌번호 포맷팅 규칙
        return when (bankCode) {
            "004" -> { // KB국민은행
                if (normalized.length == 14) {
                    // 국민 14자리 000000-00-000000
                    val part1 = normalized.substring(0, 6)
                    val part2 = normalized.substring(6, 8)
                    val part3 = normalized.substring(8)
                    "$part1-$part2-$part3"
                } else {
                    normalized
                }
            }
            "088" -> { // 신한은행
                if (normalized.length == 11 || normalized.length == 12) {
                    // 신한 11-12자리 XXX-XX-XXXXXX
                    val part1 = normalized.substring(0, 3)
                    val part2 = normalized.substring(3, 5)
                    val part3 = normalized.substring(5)
                    "$part1-$part2-$part3"
                } else {
                    normalized
                }
            }
            "081" -> { // 하나은행
                if (normalized.length == 14) {
                    // 하나은행 14자리 XXX-XXXXXX-XXXXX
                    val part1 = normalized.substring(0, 3)
                    val part2 = normalized.substring(3, 9)
                    val part3 = normalized.substring(9)
                    "$part1-$part2-$part3"
                } else {
                    normalized
                }
            }
            else -> normalized
        }
    }
} 