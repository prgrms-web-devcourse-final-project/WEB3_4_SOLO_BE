package com.pleasybank.domain.account.repository

import com.pleasybank.domain.account.entity.Account
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.Optional

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Account>
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): List<Account>
    
    fun findByAccountNumber(accountNumber: String): Optional<Account>
    
    fun findByAccountNumberAndBank(accountNumber: String, bank: String): Optional<Account>
    
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    fun findActiveAccountsByUserId(userId: Long): List<Account>
    
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    fun sumBalanceByUserId(userId: Long): BigDecimal?
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    fun countActiveAccountsByUserId(userId: Long): Long
} 