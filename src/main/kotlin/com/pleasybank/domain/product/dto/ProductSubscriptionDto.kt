package com.pleasybank.domain.product.dto

import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.product.entity.FinancialProduct
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

// 상품 구독 조회 응답 DTO
data class ProductSubscriptionResponse(
    val id: Long,
    val userId: Long,
    val userName: String? = null,
    val productId: Long,
    val productName: String,
    val productCategory: String? = null,
    val accountId: Long? = null,
    val accountNumber: String? = null,
    val amount: BigDecimal,
    val subscriptionDate: LocalDate? = null,
    val maturityDate: LocalDate? = null,
    val interestRate: BigDecimal? = null,
    val expectedReturn: BigDecimal? = null,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
    val errorMessage: String? = null
) {
    // 오류 발생 시 사용할 수 있는 간소화된 생성자
    constructor(
        id: Long,
        userId: Long,
        productId: Long,
        productName: String,
        amount: BigDecimal,
        startDate: LocalDate,
        endDate: LocalDate?,
        maturityDate: LocalDate?,
        expectedReturn: BigDecimal?,
        status: String,
        createdAt: LocalDateTime,
        errorMessage: String?
    ) : this(
        id = id,
        userId = userId,
        userName = "Unknown",
        productId = productId,
        productName = productName,
        productCategory = "UNKNOWN",
        accountId = null,
        accountNumber = null,
        amount = amount,
        subscriptionDate = startDate,
        maturityDate = maturityDate,
        interestRate = null,
        expectedReturn = expectedReturn,
        status = status,
        createdAt = createdAt,
        updatedAt = null,
        errorMessage = errorMessage
    )

    companion object {
        fun fromAccount(account: Account): ProductSubscriptionResponse {
            val product = account.product
            
            return ProductSubscriptionResponse(
                id = account.id ?: 0,
                userId = account.user.id ?: 0,
                userName = account.user.name,
                productId = product?.id ?: 0,
                productName = product?.name ?: account.accountName,
                productCategory = product?.category ?: account.accountType.toString(),
                accountId = account.id,
                accountNumber = account.accountNumber,
                amount = account.balance,
                subscriptionDate = LocalDate.from(account.createdAt),
                maturityDate = calculateMaturityDate(account, product),
                interestRate = product?.interestRate,
                expectedReturn = calculateExpectedReturn(account, product),
                status = account.status,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt
            )
        }
        
        // 만기일 계산
        private fun calculateMaturityDate(account: Account, product: FinancialProduct?): LocalDate? {
            if (product == null) return null
            
            val term = product.term ?: return null
            return LocalDate.from(account.createdAt).plusMonths(term.toLong())
        }
        
        // 예상 수익 계산
        private fun calculateExpectedReturn(account: Account, product: FinancialProduct?): BigDecimal? {
            if (product == null || product.interestRate == null) return null
            
            val principal = account.balance
            val rate = product.interestRate.divide(BigDecimal(100))
            
            // 만기일 계산
            val startDate = LocalDate.from(account.createdAt)
            val term = product.term ?: 12
            val endDate = startDate.plusMonths(term.toLong())
            
            // 단리 계산: 원금 * 이자율 * 기간(연)
            return principal.multiply(rate).multiply(BigDecimal(term)).divide(BigDecimal(12), 2, java.math.RoundingMode.HALF_UP)
        }
    }
}

// 상품 구독 수정 요청 DTO
data class UpdateProductSubscriptionRequest(
    val amount: BigDecimal? = null,
    val maturityDate: LocalDate? = null,
    val status: String? = null
) 