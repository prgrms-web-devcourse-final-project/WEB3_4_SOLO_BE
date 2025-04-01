package com.pleasybank.account.entity

import com.pleasybank.transaction.entity.Transaction
import com.pleasybank.user.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "accounts")
data class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val accountNumber: String,
    
    @Column(nullable = false)
    val accountName: String,
    
    @Column(nullable = false)
    val accountType: String, // CHECKING, SAVINGS, CREDIT
    
    @Column(nullable = false, precision = 19, scale = 4)
    var balance: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val currency: String = "KRW",
    
    @Column(nullable = false)
    val status: String = "ACTIVE", // ACTIVE, INACTIVE, BLOCKED, CLOSED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false)
    val interestRate: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    var lastActivityDate: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "sourceAccount", cascade = [CascadeType.ALL])
    val outgoingTransactions: MutableList<Transaction> = mutableListOf(),
    
    @OneToMany(mappedBy = "destinationAccount", cascade = [CascadeType.ALL])
    val incomingTransactions: MutableList<Transaction> = mutableListOf()
) 