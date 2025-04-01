package com.pleasybank.account.service

import com.pleasybank.account.dto.*
import com.pleasybank.account.entity.Account
import com.pleasybank.account.repository.AccountRepository
import com.pleasybank.exception.BadRequestException
import com.pleasybank.exception.ResourceNotFoundException
import com.pleasybank.transaction.dto.TransactionSummary
import com.pleasybank.transaction.repository.TransactionRepository
import com.pleasybank.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) {
    
    @Transactional
    fun createAccount(userId: Long, request: AccountCreateRequest): AccountDetailResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다: $userId") }

        val accountNumber = generateAccountNumber()
        
        val account = Account(
            accountNumber = accountNumber,
            accountName = request.accountName,
            accountType = request.accountType,
            balance = BigDecimal.ZERO,
            currency = request.currency ?: "KRW",
            status = "ACTIVE",
            user = user,
            interestRate = calculateInterestRate(request.accountType),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(account)
        
        return AccountDetailResponse(
            id = savedAccount.id!!,
            accountNumber = savedAccount.accountNumber,
            accountName = savedAccount.accountName,
            accountType = savedAccount.accountType,
            balance = savedAccount.balance,
            currency = savedAccount.currency,
            status = savedAccount.status,
            interestRate = savedAccount.interestRate,
            lastActivityAt = savedAccount.lastActivityDate,
            createdAt = savedAccount.createdAt
        )
    }
    
    @Transactional(readOnly = true)
    fun getAccountsByUserId(userId: Long, page: Int, size: Int): AccountListResponse {
        val pageable = PageRequest.of(page, size)
        val accountsPage = accountRepository.findByUserId(userId, pageable)
        
        val accounts = accountsPage.content.map { account ->
            AccountDetailResponse(
                id = account.id!!,
                accountNumber = account.accountNumber,
                accountName = account.accountName,
                accountType = account.accountType,
                balance = account.balance,
                currency = account.currency,
                status = account.status,
                interestRate = account.interestRate,
                lastActivityAt = account.lastActivityDate,
                createdAt = account.createdAt
            )
        }
        
        return AccountListResponse(
            accounts = accounts,
            page = accountsPage.number,
            size = accountsPage.size,
            totalElements = accountsPage.totalElements,
            totalPages = accountsPage.totalPages
        )
    }
    
    @Transactional(readOnly = true)
    fun getAccountById(accountId: Long, userId: Long): AccountDetailResponse {
        val account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다: $accountId") }
            
        return AccountDetailResponse(
            id = account.id!!,
            accountNumber = account.accountNumber,
            accountName = account.accountName,
            accountType = account.accountType,
            balance = account.balance,
            currency = account.currency,
            status = account.status,
            interestRate = account.interestRate,
            lastActivityAt = account.lastActivityDate,
            createdAt = account.createdAt
        )
    }
    
    @Transactional(readOnly = true)
    fun getAccountBalance(accountId: Long, userId: Long): AccountBalanceResponse {
        val account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다: $accountId") }
            
        return AccountBalanceResponse(
            accountId = account.id!!,
            accountNumber = account.accountNumber,
            availableBalance = account.balance,
            currency = account.currency,
            timestamp = LocalDateTime.now()
        )
    }
    
    @Transactional
    fun updateAccountStatus(accountId: Long, userId: Long, status: String): AccountDetailResponse {
        if (!isValidStatus(status)) {
            throw BadRequestException("유효하지 않은 상태입니다: $status")
        }
        
        val account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다: $accountId") }
            
        // 계좌 상태 변경
        val updatedAccount = account.copy(
            status = status, 
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(updatedAccount)
        
        return AccountDetailResponse(
            id = savedAccount.id!!,
            accountNumber = savedAccount.accountNumber,
            accountName = savedAccount.accountName,
            accountType = savedAccount.accountType,
            balance = savedAccount.balance,
            currency = savedAccount.currency,
            status = savedAccount.status,
            interestRate = savedAccount.interestRate,
            lastActivityAt = savedAccount.lastActivityDate,
            createdAt = savedAccount.createdAt
        )
    }
    
    @Transactional
    fun updateAccountBalance(accountId: Long, amount: BigDecimal): Account {
        val account = accountRepository.findById(accountId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다: $accountId") }
            
        // 잔액 변경
        val newBalance = account.balance.add(amount)
        val updatedAccount = account.copy(
            balance = newBalance,
            updatedAt = LocalDateTime.now(),
            lastActivityDate = LocalDateTime.now()
        )
        
        return accountRepository.save(updatedAccount)
    }
    
    @Transactional(readOnly = true)
    fun getAccountTransactions(
        userId: Long,
        accountId: Long,
        startDateStr: String?,
        endDateStr: String?,
        page: Int,
        size: Int
    ): AccountTransactionListResponse {
        val account = accountRepository.findByIdAndUserId(accountId, userId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다: $accountId") }
        
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        val startDate = if (startDateStr != null) {
            try {
                LocalDateTime.parse(startDateStr, dateFormatter)
            } catch (e: DateTimeParseException) {
                throw BadRequestException("시작 날짜 형식이 잘못되었습니다. 형식: yyyy-MM-dd HH:mm:ss")
            }
        } else {
            null
        }
        
        val endDate = if (endDateStr != null) {
            try {
                LocalDateTime.parse(endDateStr, dateFormatter)
            } catch (e: DateTimeParseException) {
                throw BadRequestException("종료 날짜 형식이 잘못되었습니다. 형식: yyyy-MM-dd HH:mm:ss")
            }
        } else {
            null
        }
        
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "transactionDate")
        )
        
        // 여기서는 실제 트랜잭션 데이터를 가져오는 로직이 구현되어야 합니다.
        // 지금은 간단하게 빈 목록을 반환합니다.
        
        return AccountTransactionListResponse(
            accountId = account.id!!,
            accountNumber = account.accountNumber,
            transactions = emptyList(),
            page = page,
            size = size,
            totalElements = 0,
            totalPages = 0
        )
    }

    private fun generateAccountNumber(): String {
        // 계좌번호 생성 로직 (예: 랜덤 10자리)
        val accountNumber = (100000000L..999999999L).random().toString()
        
        // 중복 검사
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            return generateAccountNumber()  // 재귀적으로 다시 생성
        }
        
        return accountNumber
    }

    private fun calculateInterestRate(accountType: String): BigDecimal {
        // 계좌 유형에 따른 이자율 계산
        return when (accountType) {
            "SAVINGS" -> BigDecimal("0.0225")  // 2.25%
            "CHECKING" -> BigDecimal("0.0025") // 0.25%
            "CREDIT" -> BigDecimal("0.1500")   // 15.00%
            else -> BigDecimal.ZERO
        }
    }

    private fun isValidStatus(status: String): Boolean {
        val validStatuses = listOf("ACTIVE", "INACTIVE", "BLOCKED", "CLOSED")
        return status in validStatuses
    }
} 