package com.pleasybank.domain.account.entity

import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 계좌 엔티티
 * 사용자의 계좌 정보를 저장하는 엔티티입니다.
 */
@Entity
@Table(name = "accounts")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "account_number", nullable = false, unique = true)
    val accountNumber: String,
    
    @Column(nullable = false)
    val bank: String,
    
    @Column(name = "account_name", nullable = false)
    val accountName: String,
    
    @Column(name = "account_type", nullable = false)
    val accountType: String,
    
    @Column(nullable = false, precision = 19, scale = 4)
    var balance: BigDecimal,
    
    @Column(name = "fintech_use_num", unique = true)
    val fintechUseNum: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var status: String = "ACTIVE"
) 