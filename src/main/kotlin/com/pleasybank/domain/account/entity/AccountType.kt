package com.pleasybank.domain.account.entity

/**
 * 계좌 유형
 */
enum class AccountType {
    /**
     * 입출금계좌 (자유입출금)
     */
    CHECKING,
    
    /**
     * 적금계좌 (예금)
     */
    SAVINGS,
    
    /**
     * 정기예금계좌
     */
    DEPOSIT,
    
    /**
     * 펀드계좌
     */
    FUND,
    
    /**
     * 대출계좌
     */
    LOAN;
    
    companion object {
        fun fromString(type: String): AccountType {
            return try {
                valueOf(type.uppercase())
            } catch (e: IllegalArgumentException) {
                CHECKING // 기본값
            }
        }
    }
} 