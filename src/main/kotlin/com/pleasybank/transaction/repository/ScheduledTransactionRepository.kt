package com.pleasybank.transaction.repository

import com.pleasybank.transaction.entity.ScheduledTransaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScheduledTransactionRepository : JpaRepository<ScheduledTransaction, Long> {
    
    @Query("SELECT st FROM ScheduledTransaction st WHERE st.sourceAccount.user.id = :userId AND st.status NOT IN :statuses")
    fun findByUserIdAndStatusNotIn(userId: Long, statuses: List<String>, pageable: Pageable): Page<ScheduledTransaction>
    
    @Query("SELECT st FROM ScheduledTransaction st WHERE st.scheduledDate <= :now AND st.status = 'SCHEDULED'")
    fun findPendingScheduledTransactions(now: java.time.LocalDateTime): List<ScheduledTransaction>
} 