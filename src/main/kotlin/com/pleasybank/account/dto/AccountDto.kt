package com.pleasybank.account.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 계좌 생성 요청 DTO
 */
data class AccountCreateRequest(
    val accountName: String,
    val accountType: String,
    val currency: String? = null
)

/**
 * 계좌 상세 정보 DTO
 */
data class AccountDetailResponse(
    val id: Long,
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val balance: BigDecimal,
    val currency: String,
    val status: String,
    val interestRate: BigDecimal?,
    val lastActivityAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    // 오픈뱅킹 관련 필드
    val fintechUseNum: String? = null,
    val bankName: String? = null,
    val bankCode: String? = null
)

/**
 * 계좌 목록 DTO
 */
data class AccountListResponse(
    val accounts: List<AccountDetailResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

/**
 * 계좌 잔액 DTO
 */
data class AccountBalanceResponse(
    val accountId: Long,
    val accountNumber: String,
    val availableBalance: BigDecimal,
    val currency: String,
    val timestamp: LocalDateTime
)

/**
 * 계좌 거래내역 항목 DTO
 */
data class AccountTransactionDto(
    val id: Long,
    val transactionType: String,
    val amount: BigDecimal,
    val description: String,
    val balanceBefore: BigDecimal,
    val balanceAfter: BigDecimal,
    val currency: String,
    val transactionDateTime: LocalDateTime,
    val status: String,
    val counterpartyName: String?,
    val counterpartyAccountNumber: String?,
    val category: String?
)

/**
 * 계좌 거래내역 목록 DTO
 */
data class AccountTransactionListResponse(
    val accountId: Long,
    val accountNumber: String,
    val transactions: List<AccountTransactionDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

// 계좌 거래 내역 요청 DTO
data class AccountTransactionListRequest(
    val accountId: Long,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
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