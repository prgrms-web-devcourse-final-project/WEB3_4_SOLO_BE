package com.pleasybank.domain.account.service

import com.pleasybank.domain.account.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

interface AccountService {
    
    // 계좌 생성 및 관리
    fun createAccount(userId: Long, request: CreateAccountRequest): AccountResponse
    
    fun getAccountById(id: Long): AccountResponse
    
    fun getUserAccounts(userId: Long, pageable: Pageable): Page<AccountResponse>
    
    fun updateAccount(id: Long, request: UpdateAccountRequest): AccountResponse
    
    fun closeAccount(id: Long): AccountResponse
    
    // 잔액 관리
    fun deposit(id: Long, amount: BigDecimal, description: String?): AccountResponse
    
    fun withdraw(id: Long, amount: BigDecimal, description: String?): AccountResponse
    
    fun transfer(fromAccountId: Long, toAccountId: Long, amount: BigDecimal, description: String?): AccountResponse
    
    fun updateBalance(id: Long, request: UpdateBalanceRequest): AccountResponse
    
    // 계좌 검증
    fun validateAccount(accountNumber: String, bank: String): Boolean
    
    fun getUserTotalBalance(userId: Long): BigDecimal
    
    // 관리자 기능
    fun getAllAccounts(): List<AccountResponse>
} 