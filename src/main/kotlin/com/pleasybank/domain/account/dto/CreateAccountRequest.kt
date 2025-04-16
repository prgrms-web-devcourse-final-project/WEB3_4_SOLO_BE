package com.pleasybank.domain.account.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

// 계좌 생성 요청 DTO
data class CreateAccountRequest @JsonCreator constructor(
    @field:NotBlank(message = "은행명은 필수입니다")
    @JsonProperty("bank")
    val bank: String,
    
    @field:NotBlank(message = "계좌명은 필수입니다")
    @JsonProperty("accountName")
    val accountName: String,
    
    @field:NotBlank(message = "계좌 유형은 필수입니다")
    @JsonProperty("accountType")
    val accountType: String, // SAVINGS, CHECKING, LOAN 등
    
    @field:NotBlank(message = "계좌번호는 필수입니다")
    @field:Pattern(regexp = "^[0-9\\-]+$", message = "계좌번호는 숫자와 하이픈(-)만 포함할 수 있습니다")
    @JsonProperty("accountNumber")
    val accountNumber: String,
    
    @field:NotNull(message = "초기 잔액은 필수입니다")
    @field:PositiveOrZero(message = "초기 잔액은 0 이상이어야 합니다")
    @JsonProperty("initialBalance")
    val initialBalance: BigDecimal,
    
    @JsonProperty("fintechUseNum")
    val fintechUseNum: String? = null,
    
    @JsonProperty("productId")
    val productId: Long? = null
) 