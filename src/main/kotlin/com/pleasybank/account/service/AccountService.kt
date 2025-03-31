package com.pleasybank.account.service

import com.pleasybank.account.dto.*
import com.pleasybank.account.repository.AccountRepository
import com.pleasybank.transaction.repository.TransactionRepository
import com.pleasybank.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) {
    
    @Transactional(readOnly = true)
    fun getAccountsList(userId: Long): AccountListResponse {
        val accounts = accountRepository.findByUserIdAndIsActiveTrue(userId)
        
        val accountSummaries = accounts.map { account ->
            AccountListResponse.AccountSummary(
                id = account.id!!,
                accountNumber = account.accountNumber,
                accountType = account.accountType,
                balance = account.balance,
                createdAt = account.createdAt
            )
        }
        
        return AccountListResponse(accounts = accountSummaries)
    }
    
    @Transactional(readOnly = true)
    fun getAccountDetail(userId: Long, accountId: Long): AccountDetailResponse {
        val account = accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("계좌를 찾을 수 없습니다.") }
        
        // 권한 확인 - 자신의 계좌만 조회 가능
        if (account.user.id != userId) {
            throw IllegalAccessException("해당 계좌에 접근 권한이 없습니다.")
        }
        
        // 마지막 거래 내역으로 마지막 활동 시간 확인
        val transactions = transactionRepository.findByAccountId(accountId)
        val lastTransaction = transactions.maxByOrNull { it.transactionDate }
        
        // 여기서는 임의로 이자율 2.5%로 설정 (실제로는 DB에서 가져오거나 다른 방식으로 계산)
        val interestRate = 2.5
        
        return AccountDetailResponse(
            id = account.id!!,
            accountNumber = account.accountNumber,
            accountType = account.accountType,
            balance = account.balance,
            interestRate = interestRate,
            createdAt = account.createdAt,
            lastActivityAt = lastTransaction?.transactionDate
        )
    }
    
    @Transactional(readOnly = true)
    fun getAccountBalance(userId: Long, accountId: Long): AccountBalanceResponse {
        val account = accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("계좌를 찾을 수 없습니다.") }
        
        // 권한 확인 - 자신의 계좌만 조회 가능
        if (account.user.id != userId) {
            throw IllegalAccessException("해당 계좌에 접근 권한이 없습니다.")
        }
        
        // 여기서는 간단하게 잔액과 가용 잔액이 동일하게 설정
        // 실제로는 미결제 거래나 보류 중인 거래 등을 고려해야 함
        val availableBalance = account.balance
        
        return AccountBalanceResponse(
            accountId = account.id!!,
            accountNumber = account.accountNumber,
            balance = account.balance,
            availableBalance = availableBalance,
            asOf = LocalDateTime.now()
        )
    }
    
    @Transactional(readOnly = true)
    fun getAccountTransactions(
        userId: Long, 
        accountId: Long, 
        request: AccountTransactionListRequest
    ): AccountTransactionListResponse {
        val account = accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("계좌를 찾을 수 없습니다.") }
        
        // 권한 확인 - 자신의 계좌만 조회 가능
        if (account.user.id != userId) {
            throw IllegalAccessException("해당 계좌에 접근 권한이 없습니다.")
        }
        
        val pageable = PageRequest.of(
            request.page,
            request.size,
            Sort.by(Sort.Direction.DESC, "transactionDate")
        )
        
        val transactionsPage = if (request.startDate != null && request.endDate != null) {
            transactionRepository.findByAccountIdAndDateRange(
                accountId,
                request.startDate,
                request.endDate,
                pageable
            )
        } else {
            transactionRepository.findByAccountId(accountId, pageable)
        }
        
        val transactionSummaries = transactionsPage.content.map { transaction ->
            // 이 계좌가 거래의 출발지인지 목적지인지에 따라 금액 및 잔액 계산 방식 변경
            val isOutgoing = transaction.sourceAccount?.id == accountId
            val amount = if (isOutgoing) transaction.amount.negate() else transaction.amount
            
            // 실제 잔액은 거래 시점의 잔액을 계산해야 하지만, 여기서는 간단하게 처리
            // 실제 구현에서는 거래 내역에 잔액 정보를 포함시키는 것이 좋음
            val balance = BigDecimal.ZERO // 임시 값
            
            AccountTransactionListResponse.TransactionSummary(
                id = transaction.id!!,
                transactionDate = transaction.transactionDate,
                amount = amount,
                transactionType = transaction.transactionType,
                description = transaction.description,
                balance = balance
            )
        }
        
        return AccountTransactionListResponse(
            content = transactionSummaries,
            totalElements = transactionsPage.totalElements,
            totalPages = transactionsPage.totalPages,
            size = transactionsPage.size,
            number = transactionsPage.number
        )
    }
} 