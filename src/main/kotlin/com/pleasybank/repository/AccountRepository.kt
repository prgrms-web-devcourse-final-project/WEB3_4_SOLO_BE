package com.pleasybank.repository

import com.pleasybank.entity.account.Account
import com.pleasybank.entity.notification.Notification
import com.pleasybank.entity.transaction.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface AccountRepository : JpaRepository<Account, Long> {
    fun findByUserId(userId: Long): List<Account>
    
    fun findByUserIdAndIsActiveTrue(userId: Long): List<Account>
    
    fun findByAccountNumber(accountNumber: String): Optional<Account>
    
    fun existsByAccountNumber(accountNumber: String): Boolean
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.accountType = :accountType")
    fun findByUserIdAndAccountType(
        @Param("userId") userId: Long, 
        @Param("accountType") accountType: String
    ): List<Account>
}

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

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Notification>
    
    fun findByUserIdAndIsRead(userId: Long, isRead: Boolean, pageable: Pageable): Page<Notification>
    
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt < :date")
    fun findByUserIdAndCreatedAtBefore(
        @Param("userId") userId: Long,
        @Param("date") date: LocalDateTime,
        pageable: Pageable
    ): Page<Notification>
} 