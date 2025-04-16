package com.pleasybank.domain.transfer.entity

import com.pleasybank.domain.account.entity.Account
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 거래 내역 엔티티
 * 계좌 간 거래 내역을 저장하는 엔티티입니다.
 */
@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    val fromAccount: Account?,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    val toAccount: Account?,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,
    
    @Column(nullable = false)
    val type: String, // DEPOSIT, WITHDRAWAL, TRANSFER
    
    @Column
    val description: String? = null,
    
    @Column(name = "transaction_datetime", nullable = false)
    val transactionDatetime: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "transaction_date", nullable = false)
    val transactionDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val status: String = "COMPLETED" // PENDING, COMPLETED, FAILED, CANCELED
) 