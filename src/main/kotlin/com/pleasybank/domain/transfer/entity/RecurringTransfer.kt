package com.pleasybank.domain.transfer.entity

import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 자동이체 설정 엔티티
 * 정기적인 자동이체 설정 정보를 저장하는 엔티티입니다.
 */
@Entity
@Table(name = "recurring_transfers")
data class RecurringTransfer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    val fromAccount: Account,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    val toAccount: Account,
    
    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,
    
    @Column(nullable = false, length = 255)
    val description: String,
    
    @Column(nullable = false, length = 20)
    val frequency: String, // DAILY, WEEKLY, MONTHLY
    
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    val dayOfWeek: DayOfWeek? = null, // WEEKLY 주기일 때 사용 (월요일=1, 일요일=7)
    
    @Column(name = "day_of_month")
    val dayOfMonth: Int? = null, // MONTHLY 주기일 때 사용 (1-31)
    
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,
    
    @Column(name = "end_date")
    val endDate: LocalDate? = null, // null이면 무기한 반복
    
    @Column(name = "next_execution_date", nullable = false)
    val nextExecutionDate: LocalDate,
    
    @Column(name = "last_execution_date")
    val lastExecutionDate: LocalDate? = null,
    
    @Column(nullable = false)
    val active: Boolean = true,
    
    @Column(nullable = false, length = 20)
    val status: String = "ACTIVE", // ACTIVE, PAUSED, CANCELLED
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 