package com.pleasybank.domain.product.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 금융 상품 엔티티
 * 은행에서 제공하는 금융 상품 정보를 저장하는 엔티티입니다.
 */
@Entity
@Table(name = "financial_products")
data class FinancialProduct(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val category: String, // SAVINGS, DEPOSIT, LOAN, FUND, etc.
    
    @Column(length = 1000)
    val description: String? = null,
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 3)
    val interestRate: BigDecimal,
    
    @Column
    val term: Int? = null, // 상품 기간(개월)
    
    @Column(name = "min_amount")
    val minAmount: BigDecimal? = null,
    
    @Column(name = "max_amount")
    val maxAmount: BigDecimal? = null,
    
    @Column(name = "features", columnDefinition = "TEXT")
    @Convert(converter = FeatureListConverter::class)
    val features: List<String> = emptyList(),
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val status: String = "ACTIVE", // ACTIVE, INACTIVE, DISCONTINUED
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "image_url")
    val imageUrl: String? = null
)

/**
 * features 필드를 JSON 형태로 변환하기 위한 컨버터
 */
@Converter
class FeatureListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return attribute?.joinToString(",") ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return dbData?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
} 