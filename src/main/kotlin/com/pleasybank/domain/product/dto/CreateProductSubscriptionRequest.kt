package com.pleasybank.domain.product.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class CreateProductSubscriptionRequest(
    @field:NotNull(message = "상품 ID는 필수입니다.")
    val productId: Long,
    
    @field:Positive(message = "금액은 양수여야 합니다.")
    val amount: BigDecimal,
    
    // 사용자 ID (컨트롤러에서 설정)
    var userId: Long = 0,
    
    // CHECKING, LOAN이 아닌 경우 필수
    val accountId: Long? = null,
    
    // term 필드 (정수로 처리되도록 함)
    val term: Int? = null,
    
    val maturityDate: LocalDate? = null
) {
    // 문자열로 표현된 term 필드도 처리할 수 있도록 추가 메서드
    fun getTermAsInt(): Int? {
        return term
    }
    
    override fun toString(): String {
        return "CreateProductSubscriptionRequest(productId=$productId, amount=$amount, userId=$userId, accountId=$accountId, term=$term, maturityDate=$maturityDate)"
    }
} 