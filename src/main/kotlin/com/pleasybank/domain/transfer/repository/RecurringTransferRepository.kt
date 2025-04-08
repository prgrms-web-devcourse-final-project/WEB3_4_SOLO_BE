package com.pleasybank.domain.transfer.repository

import com.pleasybank.domain.transfer.entity.RecurringTransfer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.List

interface RecurringTransferRepository : JpaRepository<RecurringTransfer, Long> {
    
    fun findByFromAccountUserIdAndStatus(userId: Long, status: String): List<RecurringTransfer>
    
    @Query("SELECT rt FROM RecurringTransfer rt WHERE rt.status = :status AND rt.nextExecutionDate <= :dateTime")
    fun findByStatusAndNextExecutionBefore(
        @Param("status") status: String,
        @Param("dateTime") dateTime: LocalDateTime
    ): List<RecurringTransfer>
    
    @Query("SELECT rt FROM RecurringTransfer rt WHERE rt.status = 'ACTIVE' AND rt.nextExecutionDate <= :date")
    fun findDueRecurringTransfers(@Param("date") date: LocalDate): List<RecurringTransfer>
    
    @Query("SELECT COUNT(rt) FROM RecurringTransfer rt WHERE rt.fromAccount.id = :accountId AND rt.status = 'ACTIVE'")
    fun countActiveRecurringTransfersByAccountId(@Param("accountId") accountId: Long): Long
} 