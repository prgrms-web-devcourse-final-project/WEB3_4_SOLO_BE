package com.pleasybank.domain.product.dto

import com.pleasybank.domain.product.entity.FinancialProduct
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

// 금융 상품 조회 응답 DTO
data class FinancialProductResponse(
    val id: Long,
    val name: String,
    val category: String,
    val description: String?,
    val interestRate: BigDecimal,
    val term: Int?,
    val minAmount: BigDecimal?,
    val maxAmount: BigDecimal?,
    val features: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: String,
    val isActive: Boolean,
    val imageUrl: String?
) {
    companion object {
        fun fromEntity(entity: FinancialProduct): FinancialProductResponse {
            return FinancialProductResponse(
                id = entity.id ?: 0,
                name = entity.name,
                category = entity.category,
                description = entity.description,
                interestRate = entity.interestRate,
                term = entity.term,
                minAmount = entity.minAmount,
                maxAmount = entity.maxAmount,
                features = entity.features,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                status = entity.status,
                isActive = entity.isActive,
                imageUrl = entity.imageUrl
            )
        }
    }
}

// 금융 상품 생성 요청 DTO
data class CreateFinancialProductRequest(
    @field:NotBlank(message = "상품명은 필수입니다")
    val name: String,
    
    @field:NotBlank(message = "상품 카테고리는 필수입니다")
    val category: String,
    
    val description: String? = null,
    
    @field:NotNull(message = "이자율은 필수입니다")
    @field:PositiveOrZero(message = "이자율은 0 이상이어야 합니다")
    val interestRate: BigDecimal,
    
    @field:Positive(message = "기간은 양수여야 합니다")
    val term: Int? = null,
    
    @field:PositiveOrZero(message = "최소 금액은 0 이상이어야 합니다")
    val minAmount: BigDecimal? = null,
    
    @field:Positive(message = "최대 금액은 양수여야 합니다")
    val maxAmount: BigDecimal? = null,
    
    val features: List<String> = emptyList(),
    
    val isActive: Boolean = true,
    
    val imageUrl: String? = null
) {
    fun toEntity(): FinancialProduct {
        return FinancialProduct(
            name = name,
            category = category,
            description = description,
            interestRate = interestRate,
            term = term,
            minAmount = minAmount,
            maxAmount = maxAmount,
            features = features,
            isActive = isActive,
            imageUrl = imageUrl
        )
    }
}

// 금융 상품 수정 요청 DTO
data class UpdateFinancialProductRequest(
    val name: String? = null,
    val category: String? = null,
    val description: String? = null,
    val interestRate: BigDecimal? = null,
    val term: Int? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val features: List<String>? = null,
    val status: String? = null,
    val isActive: Boolean? = null,
    val imageUrl: String? = null
) 