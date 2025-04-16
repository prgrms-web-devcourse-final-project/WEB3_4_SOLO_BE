package com.pleasybank.domain.account.dto

import java.math.BigDecimal

// 계좌 수정 요청 DTO
data class UpdateAccountRequest(
    val accountName: String? = null,
    val status: String? = null,
    val productId: Long? = null
) 