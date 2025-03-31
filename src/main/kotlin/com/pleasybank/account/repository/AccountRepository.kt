package com.pleasybank.account.repository

import com.pleasybank.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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