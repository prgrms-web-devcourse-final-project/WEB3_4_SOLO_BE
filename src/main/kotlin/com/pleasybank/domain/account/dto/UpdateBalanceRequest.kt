package com.pleasybank.domain.account.dto

import java.math.BigDecimal
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

// 잔액 변경 요청 DTO
data class UpdateBalanceRequest(
    @field:NotNull(message = "금액은 필수입니다")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "변경 유형은 필수입니다")
    val type: String, // DEPOSIT, WITHDRAWAL, TRANSFER
    
    val description: String? = null
) 