package com.pleasybank.domain.product.entity

import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 금융 상품 가입 정보 엔티티
 * 사용자가 가입한 금융 상품 정보를 저장하는 엔티티입니다.
 */
@Entity
@Table(name = "product_subscriptions")
data class ProductSubscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: FinancialProduct,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,
    
    @Column(nullable = false, precision = 15, scale = 2)
    val amount: BigDecimal,
    
    @Column(name = "subscription_date", nullable = false)
    val subscriptionDate: LocalDate = LocalDate.now(),
    
    @Column(name = "maturity_date", nullable = false)
    val maturityDate: LocalDate,
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 3)
    val interestRate: BigDecimal,
    
    @Column(name = "expected_return", precision = 15, scale = 2)
    val expectedReturn: BigDecimal? = null,
    
    @Column(name = "auto_renew")
    val autoRenew: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val status: String = "ACTIVE" // ACTIVE, MATURED, CANCELLED
) 