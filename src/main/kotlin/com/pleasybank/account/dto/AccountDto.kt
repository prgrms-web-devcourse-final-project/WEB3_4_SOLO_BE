package com.pleasybank.account.dto

import java.math.BigDecimal
import java.time.LocalDateTime

// 계좌 생성 요청 DTO
data class AccountCreateRequest(
    val accountName: String,
    val accountType: String, // CHECKING, SAVINGS, CREDIT
    val currency: String? = "KRW"
)

// 계좌 목록 응답 DTO
data class AccountListResponse(
    val accounts: List<AccountDetailResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

// 계좌 상세 정보 응답 DTO
data class AccountDetailResponse(
    val id: Long,
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val balance: BigDecimal,
    val currency: String,
    val status: String,
    val interestRate: BigDecimal,
    val lastActivityAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

// 계좌 잔액 응답 DTO
data class AccountBalanceResponse(
    val accountId: Long,
    val accountNumber: String,
    val availableBalance: BigDecimal,
    val currency: String,
    val timestamp: LocalDateTime
)

// 계좌 거래 내역 요청 DTO
data class AccountTransactionListRequest(
    val accountId: Long,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
)

// 계좌 거래 내역 응답 DTO
data class AccountTransactionListResponse(
    val accountId: Long,
    val accountNumber: String,
    val transactions: List<TransactionSummary>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

// 거래 요약 DTO (계좌 컨텍스트에서 사용)
data class TransactionSummary(
    val id: Long,
    val transactionNumber: String,
    val counterpartyName: String?,
    val counterpartyAccountNumber: String?,
    val amount: BigDecimal,
    val transactionType: String,
    val description: String?,
    val transactionDate: LocalDateTime,
    val isDebit: Boolean
) 