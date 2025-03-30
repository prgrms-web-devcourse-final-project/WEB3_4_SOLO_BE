package com.pleasybank.entity.transaction

import com.pleasybank.entity.account.Account
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, name = "transaction_number")
    val transactionNumber: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    val sourceAccount: Account? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    val destinationAccount: Account? = null,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,
    
    @Column(nullable = false, name = "transaction_type")
    val transactionType: String,
    
    val description: String? = null,
    
    @Column(nullable = false)
    val status: String,
    
    @Column(nullable = false, name = "transaction_date")
    val transactionDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) 