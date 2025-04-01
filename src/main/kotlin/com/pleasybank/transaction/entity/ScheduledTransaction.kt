package com.pleasybank.transaction.entity

import com.pleasybank.account.entity.Account
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "scheduled_transactions")
data class ScheduledTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    val sourceAccount: Account,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    val destinationAccount: Account,
    
    val amount: BigDecimal,
    
    val description: String? = null,
    
    val scheduledDate: LocalDateTime,
    
    val isRecurring: Boolean = false,
    
    val recurringPeriod: String? = null, // DAILY, WEEKLY, MONTHLY 등
    
    var status: String, // SCHEDULED, COMPLETED, CANCELLED, FAILED
    
    val createdAt: LocalDateTime,
    
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    var executedAt: LocalDateTime? = null
) 