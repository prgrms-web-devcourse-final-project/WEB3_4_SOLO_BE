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
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val transactionDate: LocalDateTime,
    
    val sourceAccountId: Long?,
    val sourceAccountNumber: String?,
    val sourceAccountName: String?,
    
    val destinationAccountId: Long?,
    val destinationAccountNumber: String?,
    val destinationAccountName: String?,
    
    val amount: BigDecimal,
    val fee: BigDecimal,
    val description: String?,
    val transactionType: String,
    val status: String,
    val referenceNumber: String
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