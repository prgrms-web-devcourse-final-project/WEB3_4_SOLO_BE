package com.pleasybank.transaction.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

// 거래(이체) 요청 DTO
data class TransferRequest(
    val sourceAccountId: Long,
    val destinationAccountNumber: String,
    val amount: BigDecimal,
    val description: String?,
    val pinOrPassword: String // PIN 또는 비밀번호 검증용
)

// 거래(이체) 응답 DTO
data class TransferResponse(
    val transactionId: Long,
    val sourceAccountId: Long,
    val sourceAccountNumber: String,
    val destinationAccountNumber: String,
    val amount: BigDecimal,
    val fee: BigDecimal,
    val description: String?,
    val status: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val transactionDate: LocalDateTime,
    
    val remainingBalance: BigDecimal,
    val message: String?
)

// 거래 상세 조회 응답 DTO
data class TransactionDetailResponse(
    val id: Long,
    val transactionType: String,
    val sourceAccountId: Long,
    val sourceAccountNumber: String,
    val sourceBank: String? = null,
    val destinationAccountId: Long? = null,
    val destinationAccountNumber: String? = null,
    val destinationBank: String? = null,
    val amount: BigDecimal,
    val currency: String,
    val description: String,
    val status: String,
    val feeAmount: BigDecimal? = null,
    val transactionDateTime: LocalDateTime,
    val apiTransactionId: String? = null
)

// 거래 취소 요청 DTO
data class TransactionCancelRequest(
    val transactionId: Long,
    val reason: String?,
    val pinOrPassword: String // PIN 또는 비밀번호 검증용
)

// 거래 취소 응답 DTO
data class TransactionCancelResponse(
    val transactionId: Long,
    val originalTransactionId: Long,
    val status: String,
    val message: String?
)

// 예약 이체 요청 DTO
data class ScheduledTransferRequest(
    val sourceAccountId: Long,
    val destinationAccountNumber: String,
    val amount: BigDecimal,
    val description: String?,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val scheduledDate: LocalDateTime,
    
    val isRecurring: Boolean,
    val recurringPeriod: String?, // "DAILY", "WEEKLY", "MONTHLY" 등
    val pinOrPassword: String // PIN 또는 비밀번호 검증용
)

// 예약 이체 응답 DTO
data class ScheduledTransferResponse(
    val scheduledTransactionId: Long,
    val sourceAccountId: Long,
    val destinationAccountNumber: String,
    val amount: BigDecimal,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val scheduledDate: LocalDateTime,
    
    val isRecurring: Boolean,
    val recurringPeriod: String?,
    val status: String,
    val message: String?
)

// 거래 생성 요청 DTO
data class TransactionCreateRequest(
    val sourceAccountId: Long? = null,
    val destinationAccountId: Long? = null,
    val amount: BigDecimal? = null,
    val description: String? = null,
    
    val fintechUseNum: String? = null,
    val receiverName: String? = null,
    val receiverBankCode: String? = null,
    val receiverAccountNum: String? = null
)

// 거래 목록 요청 DTO
data class TransactionListRequest(
    val accountId: Long? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val type: String? = null,
    val page: Int = 0,
    val size: Int = 20
)

// 거래 목록 응답 DTO
data class TransactionListResponse(
    val transactions: List<TransactionDetailResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

// 거래 요약 DTO
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