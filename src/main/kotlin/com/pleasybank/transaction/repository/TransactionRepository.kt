package com.pleasybank.transaction.repository

import com.pleasybank.transaction.entity.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findByTransactionNumber(transactionNumber: String): Optional<Transaction>
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId")
    fun findByAccountId(@Param("accountId") accountId: Long, pageable: Pageable): Page<Transaction>
    
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId")
    fun findByAccountId(@Param("accountId") accountId: Long): List<Transaction>
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) AND " +
           "t.transactionDate BETWEEN :startDate AND :endDate")
    fun findByAccountIdAndDateRange(
        @Param("accountId") accountId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Transaction>
    
    @Query("SELECT t FROM Transaction t " +
           "WHERE (t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId) " +
           "AND t.transactionType = :transactionType")
    fun findByUserIdAndTransactionType(
        @Param("userId") userId: Long,
        @Param("transactionType") transactionType: String,
        pageable: Pageable
    ): Page<Transaction>
    
    @Query("SELECT t FROM Transaction t " +
           "WHERE (t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    fun findByUserIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Transaction>
} 