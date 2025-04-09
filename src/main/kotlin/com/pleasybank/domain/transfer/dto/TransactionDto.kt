package com.pleasybank.domain.transfer.dto

import com.pleasybank.domain.transfer.entity.Transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

// 거래 생성 요청 DTO
data class CreateTransactionRequest(
    val fromAccountId: Long? = null,
    
    val toAccountId: Long? = null,
    
    @field:NotNull(message = "금액은 필수입니다")
    @field:Positive(message = "금액은 양수여야 합니다")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "거래 유형은 필수입니다")
    val type: String, // DEPOSIT, WITHDRAWAL, TRANSFER
    
    val description: String? = null
)

// 거래 조회 응답 DTO
data class TransactionResponse(
    val id: Long,
    val fromAccountId: Long?,
    val fromAccountNumber: String?,
    val fromAccountBank: String?,
    val toAccountId: Long?,
    val toAccountNumber: String?,
    val toAccountBank: String?,
    val amount: BigDecimal,
    val type: String,
    val description: String?,
    val transactionDatetime: LocalDateTime,
    val status: String,
    val fee: BigDecimal?, // 수수료
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(transaction: Transaction): TransactionResponse {
            return TransactionResponse(
                id = transaction.id!!,
                fromAccountId = transaction.fromAccount?.id,
                fromAccountNumber = transaction.fromAccount?.accountNumber,
                fromAccountBank = transaction.fromAccount?.bank,
                toAccountId = transaction.toAccount?.id,
                toAccountNumber = transaction.toAccount?.accountNumber,
                toAccountBank = transaction.toAccount?.bank,
                amount = transaction.amount,
                type = transaction.type,
                description = transaction.description,
                transactionDatetime = transaction.transactionDatetime,
                status = transaction.status,
                fee = calculateFee(transaction),
                createdAt = transaction.createdAt,
                updatedAt = transaction.updatedAt
            )
        }
        
        @Suppress("UNUSED_PARAMETER")
        private fun calculateFee(transaction: Transaction): BigDecimal? {
            // 수수료 계산 로직 (현재는 항상 0)
            // 나중에 거래 금액이나 유형에 따라 수수료를 계산하기 위해 transaction 매개변수를 유지함
            return BigDecimal.ZERO
        }
    }
}

// 거래 요약 응답 DTO
data class TransactionSummaryResponse(
    val totalDeposit: BigDecimal,
    val totalWithdrawal: BigDecimal,
    val totalTransferIn: BigDecimal,
    val totalTransferOut: BigDecimal,
    val netChange: BigDecimal
)

class TransactionDto {

    data class TransferRequest(
        @field:NotNull(message = "출금 계좌 ID는 필수입니다.")
        val fromAccountId: Long,
        
        @field:NotNull(message = "입금 계좌 ID는 필수입니다.")
        val toAccountId: Long,
        
        @field:NotNull(message = "이체 금액은 필수입니다.")
        @field:Min(value = 100, message = "최소 이체 금액은 100원입니다.")
        val amount: BigDecimal,
        
        @field:NotBlank(message = "거래 설명은 필수입니다.")
        val description: String
    )
    
    data class DepositRequest(
        @field:NotNull(message = "계좌 ID는 필수입니다.")
        val accountId: Long,
        
        @field:NotNull(message = "입금 금액은 필수입니다.")
        @field:Min(value = 100, message = "최소 입금 금액은 100원입니다.")
        val amount: BigDecimal,
        
        @field:NotBlank(message = "거래 설명은 필수입니다.")
        val description: String
    )
    
    data class WithdrawRequest(
        @field:NotNull(message = "계좌 ID는 필수입니다.")
        val accountId: Long,
        
        @field:NotNull(message = "출금 금액은 필수입니다.")
        @field:Min(value = 100, message = "최소 출금 금액은 100원입니다.")
        val amount: BigDecimal,
        
        @field:NotBlank(message = "거래 설명은 필수입니다.")
        val description: String
    )
    
    data class Response(
        val id: Long,
        val fromAccountId: Long?,
        val fromAccountNumber: String?,
        val toAccountId: Long?,
        val toAccountNumber: String?,
        val amount: BigDecimal,
        val type: String,
        val description: String,
        val transactionDatetime: LocalDateTime,
        val status: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun fromEntity(transaction: Transaction): Response {
                return Response(
                    id = transaction.id!!,
                    fromAccountId = transaction.fromAccount?.id,
                    fromAccountNumber = transaction.fromAccount?.accountNumber,
                    toAccountId = transaction.toAccount?.id,
                    toAccountNumber = transaction.toAccount?.accountNumber,
                    amount = transaction.amount,
                    type = transaction.type,
                    description = transaction.description ?: "",
                    transactionDatetime = transaction.transactionDatetime,
                    status = transaction.status,
                    createdAt = transaction.createdAt,
                    updatedAt = transaction.updatedAt
                )
            }
        }
    }
    
    data class TransactionSummary(
        val totalDeposit: BigDecimal,
        val totalWithdrawal: BigDecimal,
        val totalTransferIn: BigDecimal,
        val totalTransferOut: BigDecimal,
        val netChange: BigDecimal
    )
} 