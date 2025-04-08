package com.pleasybank.domain.account.dto

import com.pleasybank.domain.account.entity.Account
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

// 계좌 생성 요청 DTO
data class CreateAccountRequest(
    @field:NotBlank(message = "은행명은 필수입니다")
    val bank: String,
    
    @field:NotBlank(message = "계좌명은 필수입니다")
    val accountName: String,
    
    @field:NotBlank(message = "계좌 유형은 필수입니다")
    val accountType: String, // SAVINGS, CHECKING, LOAN 등
    
    @field:NotBlank(message = "계좌번호는 필수입니다")
    @field:Pattern(regexp = "^[0-9\\-]+$", message = "계좌번호는 숫자와 하이픈(-)만 포함할 수 있습니다")
    val accountNumber: String,
    
    @field:NotNull(message = "초기 잔액은 필수입니다")
    @field:Positive(message = "초기 잔액은 양수여야 합니다")
    val initialBalance: BigDecimal,
    
    val fintechUseNum: String? = null
)

// 계좌 수정 요청 DTO
data class UpdateAccountRequest(
    val accountName: String? = null,
    val status: String? = null
)

// 계좌 조회 응답 DTO
data class AccountResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val bank: String,
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val balance: BigDecimal,
    val fintechUseNum: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(account: Account): AccountResponse {
            return AccountResponse(
                id = account.id!!,
                userId = account.user.id!!,
                userName = account.user.name,
                bank = account.bank,
                accountNumber = account.accountNumber,
                accountName = account.accountName,
                accountType = account.accountType,
                balance = account.balance,
                fintechUseNum = account.fintechUseNum,
                status = account.status,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt
            )
        }
    }
}

// 잔액 변경 요청 DTO
data class UpdateBalanceRequest(
    @field:NotNull(message = "금액은 필수입니다")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "변경 유형은 필수입니다")
    val type: String, // DEPOSIT, WITHDRAWAL, TRANSFER
    
    val description: String? = null
) 