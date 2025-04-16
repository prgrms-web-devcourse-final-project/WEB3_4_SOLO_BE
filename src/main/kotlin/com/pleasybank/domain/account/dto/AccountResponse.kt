package com.pleasybank.domain.account.dto

import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.entity.AccountType
import java.math.BigDecimal
import java.time.LocalDateTime

data class AccountResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val accountNumber: String,
    val accountName: String,
    val accountType: String,
    val bank: String,
    val balance: BigDecimal,
    val status: String,
    val fintechUseNum: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val productId: Long?,
    val productName: String?,
    val interestRate: BigDecimal?
) {
    companion object {
        fun fromEntity(account: Account): AccountResponse {
            return AccountResponse(
                id = account.id!!,
                userId = account.user.id!!,
                userName = account.user.name,
                accountNumber = account.accountNumber,
                accountName = account.accountName,
                accountType = account.accountType.toString(),
                bank = account.bank,
                balance = account.balance,
                status = account.status,
                fintechUseNum = account.fintechUseNum,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt,
                productId = account.product?.id,
                productName = account.product?.name,
                interestRate = account.product?.interestRate
            )
        }
    }
} 