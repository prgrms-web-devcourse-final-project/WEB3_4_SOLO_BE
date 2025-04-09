package com.pleasybank.domain.product.dto

import com.pleasybank.domain.product.entity.ProductSubscription
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

// 상품 구독 조회 응답 DTO
data class ProductSubscriptionResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val productId: Long,
    val productName: String,
    val productCategory: String,
    val accountId: Long,
    val accountNumber: String,
    val amount: BigDecimal,
    val subscriptionDate: LocalDate,
    val maturityDate: LocalDate,
    val interestRate: BigDecimal,
    val expectedReturn: BigDecimal?,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(entity: ProductSubscription): ProductSubscriptionResponse {
            return ProductSubscriptionResponse(
                id = entity.id ?: 0,
                userId = entity.user.id ?: 0,
                userName = entity.user.name,
                productId = entity.product.id ?: 0,
                productName = entity.product.name,
                productCategory = entity.product.category,
                accountId = entity.account.id ?: 0,
                accountNumber = entity.account.accountNumber,
                amount = entity.amount,
                subscriptionDate = entity.subscriptionDate,
                maturityDate = entity.maturityDate,
                interestRate = entity.interestRate,
                expectedReturn = entity.expectedReturn,
                status = entity.status,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}

// 상품 구독 생성 요청 DTO
data class CreateProductSubscriptionRequest(
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,
    
    @field:NotNull(message = "계좌 ID는 필수입니다")
    val accountId: Long,
    
    @field:NotNull(message = "가입 금액은 필수입니다")
    @field:Positive(message = "가입 금액은 양수여야 합니다")
    val amount: BigDecimal,
    
    @field:NotNull(message = "만기일은 필수입니다")
    val maturityDate: LocalDate
)

// 상품 구독 수정 요청 DTO
data class UpdateProductSubscriptionRequest(
    val amount: BigDecimal? = null,
    val maturityDate: LocalDate? = null,
    val status: String? = null
) 