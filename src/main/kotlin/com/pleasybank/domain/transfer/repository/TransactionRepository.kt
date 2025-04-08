package com.pleasybank.domain.transfer.repository

import com.pleasybank.domain.transfer.entity.Transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime

interface TransactionRepository : JpaRepository<Transaction, Long> {
    
    /**
     * 특정 계좌의 모든 거래내역 조회 (입금 또는 출금)
     */
    fun findByFromAccountIdOrToAccountId(
        fromAccountId: Long,
        toAccountId: Long,
        pageable: Pageable
    ): Page<Transaction>
    
    /**
     * 특정 사용자의 모든 거래내역 조회
     */
    @Query("""
        SELECT t FROM Transaction t 
        LEFT JOIN t.fromAccount fa 
        LEFT JOIN t.toAccount ta 
        LEFT JOIN fa.user fu 
        LEFT JOIN ta.user tu 
        WHERE fu.id = :userId OR tu.id = :userId
        ORDER BY t.transactionDate DESC
    """)
    fun findByUserId(@Param("userId") userId: Long, pageable: Pageable): Page<Transaction>
    
    /**
     * 특정 계좌의 특정 기간 내 거래내역 조회
     */
    fun findByFromAccountIdOrToAccountIdAndTransactionDatetimeBetween(
        fromAccountId: Long,
        toAccountId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Transaction>
    
    /**
     * 특정 계좌 목록에 특정 유형의 입금 합계 조회
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t 
        WHERE t.type = :type AND t.toAccount.id IN :accountIds 
        AND t.transactionDatetime BETWEEN :startDate AND :endDate
    """)
    fun sumByTypeAndToAccountIdInAndDateBetween(
        @Param("type") type: String,
        @Param("accountIds") accountIds: List<Long>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): BigDecimal?
    
    /**
     * 특정 계좌 목록에 특정 유형의 출금 합계 조회
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t 
        WHERE t.type = :type AND t.fromAccount.id IN :accountIds 
        AND t.transactionDatetime BETWEEN :startDate AND :endDate
    """)
    fun sumByTypeAndFromAccountIdInAndDateBetween(
        @Param("type") type: String,
        @Param("accountIds") accountIds: List<Long>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): BigDecimal?
    
    /**
     * 계좌 목록 간 이체 입금 합계 조회 (외부에서 내부로)
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t 
        WHERE t.type = :type AND t.toAccount.id IN :toAccountIds 
        AND (t.fromAccount IS NULL OR t.fromAccount.id NOT IN :excludeFromAccountIds) 
        AND t.transactionDatetime BETWEEN :startDate AND :endDate
    """)
    fun sumByTypeAndToAccountIdInAndFromAccountIdNotInAndDateBetween(
        @Param("type") type: String,
        @Param("toAccountIds") toAccountIds: List<Long>,
        @Param("excludeFromAccountIds") excludeFromAccountIds: List<Long>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): BigDecimal?
    
    /**
     * 계좌 목록 간 이체 출금 합계 조회 (내부에서 외부로)
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t 
        WHERE t.type = :type AND t.fromAccount.id IN :fromAccountIds 
        AND (t.toAccount IS NULL OR t.toAccount.id NOT IN :excludeToAccountIds) 
        AND t.transactionDatetime BETWEEN :startDate AND :endDate
    """)
    fun sumByTypeAndFromAccountIdInAndToAccountIdNotInAndDateBetween(
        @Param("type") type: String,
        @Param("fromAccountIds") fromAccountIds: List<Long>,
        @Param("excludeToAccountIds") excludeToAccountIds: List<Long>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): BigDecimal?
} 