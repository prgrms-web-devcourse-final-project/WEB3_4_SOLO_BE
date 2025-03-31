package com.pleasybank.account.dto

import java.math.BigDecimal
import java.time.LocalDateTime

// 계좌 목록 응답
data class AccountListResponse(
    val accounts: List<AccountSummary>
) {
    data class AccountSummary(
        val id: Long,
        val accountNumber: String,
        val accountType: String,
        val balance: BigDecimal,
        val createdAt: LocalDateTime
    )
}

// 계좌 상세 응답
data class AccountDetailResponse(
    val id: Long,
    val accountNumber: String,
    val accountType: String,
    val balance: BigDecimal,
    val interestRate: Double,
    val createdAt: LocalDateTime,
    val lastActivityAt: LocalDateTime?
)

// 계좌 잔액 응답
data class AccountBalanceResponse(
    val accountId: Long,
    val accountNumber: String,
    val balance: BigDecimal,
    val availableBalance: BigDecimal,
    val asOf: LocalDateTime
)

// 계좌 거래 내역 요청
data class AccountTransactionListRequest(
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
)

// 계좌 거래 내역 응답
data class AccountTransactionListResponse(
    val content: List<TransactionSummary>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int
) {
    data class TransactionSummary(
        val id: Long,
        val transactionDate: LocalDateTime,
        val amount: BigDecimal,
        val transactionType: String,
        val description: String?,
        val balance: BigDecimal
    )
} 