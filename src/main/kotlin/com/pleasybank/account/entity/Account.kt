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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false, unique = true, name = "account_number")
    val accountNumber: String,
    
    @Column(nullable = false, name = "account_name")
    val accountName: String,
    
    @Column(nullable = false, precision = 19, scale = 4)
    var balance: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false, name = "account_type")
    val accountType: String,
    
    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "sourceAccount", cascade = [CascadeType.ALL])
    val outgoingTransactions: MutableList<Transaction> = mutableListOf(),
    
    @OneToMany(mappedBy = "destinationAccount", cascade = [CascadeType.ALL])
    val incomingTransactions: MutableList<Transaction> = mutableListOf()
) 