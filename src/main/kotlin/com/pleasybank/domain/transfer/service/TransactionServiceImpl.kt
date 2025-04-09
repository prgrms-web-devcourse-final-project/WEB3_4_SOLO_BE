package com.pleasybank.domain.transfer.service

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.repository.AccountRepository
import com.pleasybank.domain.transfer.dto.*
import com.pleasybank.domain.transfer.entity.Transaction
import com.pleasybank.domain.transfer.repository.TransactionRepository
import com.pleasybank.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) : TransactionService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional(readOnly = true)
    override fun getTransactionById(userId: Long, id: Long): TransactionDto.Response {
        val transaction = transactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("거래 내역을 찾을 수 없습니다. ID: $id") }
        
        // 사용자 소유의 거래 내역인지 확인
        val fromAccountUserId = transaction.fromAccount?.user?.id
        val toAccountUserId = transaction.toAccount?.user?.id
        
        if (fromAccountUserId != userId && toAccountUserId != userId) {
            throw ResourceNotFoundException("해당 사용자의 거래 내역이 아닙니다. 거래 ID: $id")
        }
        
        return TransactionDto.Response.fromEntity(transaction)
    }

    @Transactional(readOnly = true)
    override fun getAccountTransactions(userId: Long, accountId: Long, pageable: Pageable): Page<TransactionDto.Response> {
        // 사용자 소유 계좌 확인
        validateUserAccount(userId, accountId)
        
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable)
            .map { TransactionDto.Response.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getUserTransactions(userId: Long, pageable: Pageable): Page<TransactionDto.Response> {
        // 사용자 존재 여부 확인
        validateUserExists(userId)
        
        return transactionRepository.findByUserId(userId, pageable)
            .map { TransactionDto.Response.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getTransactionsByDateRange(
        userId: Long,
        accountId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<TransactionDto.Response> {
        // 사용자 소유 계좌 확인
        validateUserAccount(userId, accountId)
        
        return transactionRepository.findByFromAccountIdOrToAccountIdAndTransactionDatetimeBetween(
            accountId, accountId, startDate, endDate, pageable
        ).map { TransactionDto.Response.fromEntity(it) }
    }

    @Transactional(readOnly = true)
    override fun getUserTransactionSummary(
        userId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): TransactionDto.TransactionSummary {
        // 사용자 존재 여부 확인
        validateUserExists(userId)
        
        val accountIds = accountRepository.findByUserId(userId).mapNotNull { it.id }
        
        if (accountIds.isEmpty()) {
            logger.info("사용자 {}의 계좌가 없습니다. 빈 거래 요약 반환", userId)
            return createEmptyTransactionSummary()
        }
        
        // 입금 합계 (DEPOSIT 또는 다른 계좌로부터의 입금)
        val totalDeposit = transactionRepository.sumByTypeAndToAccountIdInAndDateBetween(
            "DEPOSIT", accountIds, startDate, endDate
        ) ?: BigDecimal.ZERO
        
        // 출금 합계 (WITHDRAWAL)
        val totalWithdrawal = transactionRepository.sumByTypeAndFromAccountIdInAndDateBetween(
            "WITHDRAWAL", accountIds, startDate, endDate
        ) ?: BigDecimal.ZERO
        
        // 계좌 간 이체 - 입금 (다른 계좌로부터의 입금)
        val totalTransferIn = transactionRepository.sumByTypeAndToAccountIdInAndFromAccountIdNotInAndDateBetween(
            "TRANSFER", accountIds, accountIds, startDate, endDate
        ) ?: BigDecimal.ZERO
        
        // 계좌 간 이체 - 출금 (다른 계좌로의 출금)
        val totalTransferOut = transactionRepository.sumByTypeAndFromAccountIdInAndToAccountIdNotInAndDateBetween(
            "TRANSFER", accountIds, accountIds, startDate, endDate
        ) ?: BigDecimal.ZERO
        
        // 순 변화량 = 총 입금 - 총 출금 + 이체 입금 - 이체 출금
        val netChange = totalDeposit.subtract(totalWithdrawal).add(totalTransferIn).subtract(totalTransferOut)
        
        logger.debug("사용자 {}의 거래 요약: 입금={}, 출금={}, 이체입금={}, 이체출금={}, 순변화={}", 
            userId, totalDeposit, totalWithdrawal, totalTransferIn, totalTransferOut, netChange)
        
        return TransactionDto.TransactionSummary(
            totalDeposit = totalDeposit,
            totalWithdrawal = totalWithdrawal,
            totalTransferIn = totalTransferIn,
            totalTransferOut = totalTransferOut,
            netChange = netChange
        )
    }
    
    @Transactional(readOnly = true)
    override fun getAccountBalance(userId: Long, accountId: Long): BigDecimal {
        // 계좌 존재 여부 확인 및 사용자 소유 여부 확인
        val account = validateUserAccount(userId, accountId)
        return account.balance
    }

    @Transactional
    override fun transfer(userId: Long, request: TransactionDto.TransferRequest): TransactionDto.Response {
        // 출금 계좌 확인
        val fromAccount = findAccountById(request.fromAccountId)
        
        // 입금 계좌 확인
        val toAccount = findAccountById(request.toAccountId)
        
        // 계좌 소유자 확인
        validateAccountOwnership(userId, fromAccount)
        
        // 계좌 잔액 확인
        validateSufficientBalance(fromAccount, request.amount)
        
        // 트랜잭션 생성
        val transaction = createTransaction(
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = request.amount,
            type = TransactionType.TRANSFER.name,
            description = request.description
        )
        
        // 계좌 잔액 업데이트
        updateAccountBalance(fromAccount, request.amount.negate())
        updateAccountBalance(toAccount, request.amount)
        
        logger.info("이체 완료: 출금계좌={}, 입금계좌={}, 금액={}", fromAccount.id, toAccount.id, request.amount)
        
        return TransactionDto.Response.fromEntity(transaction)
    }

    @Transactional
    override fun deposit(userId: Long, request: TransactionDto.DepositRequest): TransactionDto.Response {
        // 입금 계좌 확인
        val account = findAccountById(request.accountId)
        
        // 계좌 소유자 확인
        validateAccountOwnership(userId, account)
        
        // 트랜잭션 생성
        val transaction = createTransaction(
            fromAccount = null,
            toAccount = account,
            amount = request.amount,
            type = TransactionType.DEPOSIT.name,
            description = request.description
        )
        
        // 계좌 잔액 업데이트
        updateAccountBalance(account, request.amount)
        
        logger.info("입금 완료: 계좌={}, 금액={}", account.id, request.amount)
        
        return TransactionDto.Response.fromEntity(transaction)
    }

    @Transactional
    override fun withdraw(userId: Long, request: TransactionDto.WithdrawRequest): TransactionDto.Response {
        // 출금 계좌 확인
        val account = findAccountById(request.accountId)
        
        // 계좌 소유자 확인
        validateAccountOwnership(userId, account)
        
        // 계좌 잔액 확인
        validateSufficientBalance(account, request.amount)
        
        // 트랜잭션 생성
        val transaction = createTransaction(
            fromAccount = account,
            toAccount = null,
            amount = request.amount,
            type = TransactionType.WITHDRAWAL.name,
            description = request.description
        )
        
        // 계좌 잔액 업데이트
        updateAccountBalance(account, request.amount.negate())
        
        logger.info("출금 완료: 계좌={}, 금액={}", account.id, request.amount)
        
        return TransactionDto.Response.fromEntity(transaction)
    }
    
    // 헬퍼 메서드
    private fun createEmptyTransactionSummary() = TransactionDto.TransactionSummary(
        totalDeposit = BigDecimal.ZERO,
        totalWithdrawal = BigDecimal.ZERO,
        totalTransferIn = BigDecimal.ZERO,
        totalTransferOut = BigDecimal.ZERO,
        netChange = BigDecimal.ZERO
    )
    
    private fun validateUserExists(userId: Long) {
        if (!userRepository.existsById(userId)) {
            logger.warn("사용자를 찾을 수 없음: ID={}", userId)
            throw ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: $userId")
        }
    }
    
    private fun findAccountById(accountId: Long): Account {
        return accountRepository.findById(accountId)
            .orElseThrow { 
                logger.warn("계좌를 찾을 수 없음: ID={}", accountId)
                ResourceNotFoundException("계좌를 찾을 수 없습니다. ID: $accountId") 
            }
    }
    
    private fun validateUserAccount(userId: Long, accountId: Long): Account {
        val account = findAccountById(accountId)
        
        if (account.user.id != userId) {
            logger.warn("사용자({})의 계좌가 아님: 계좌ID={}", userId, accountId)
            throw ResourceNotFoundException("해당 사용자의 계좌가 아닙니다. 계좌 ID: $accountId")
        }
        
        return account
    }
    
    private fun validateAccountOwnership(userId: Long, account: Account) {
        if (account.user.id != userId) {
            logger.warn("사용자({})의 계좌가 아님: 계좌ID={}", userId, account.id)
            throw IllegalArgumentException("해당 사용자의 계좌가 아닙니다. 계좌 ID: ${account.id}")
        }
    }
    
    private fun validateSufficientBalance(account: Account, amount: BigDecimal) {
        if (account.balance < amount) {
            logger.warn("잔액 부족: 계좌ID={}, 잔액={}, 필요금액={}", account.id, account.balance, amount)
            throw IllegalArgumentException("잔액이 부족합니다. 현재 잔액: ${account.balance}, 필요 금액: $amount")
        }
    }
    
    private fun createTransaction(
        fromAccount: Account?, 
        toAccount: Account?, 
        amount: BigDecimal, 
        type: String, 
        description: String
    ): Transaction {
        val transaction = Transaction(
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = amount,
            type = type,
            description = description,
            transactionDatetime = LocalDateTime.now(),
            status = TransactionStatus.COMPLETED.name
        )
        
        return transactionRepository.save(transaction)
    }
    
    private fun updateAccountBalance(account: Account, amountChange: BigDecimal) {
        account.balance = account.balance.add(amountChange)
        accountRepository.save(account)
        logger.debug("계좌 잔액 업데이트: 계좌ID={}, 변경금액={}, 새잔액={}", account.id, amountChange, account.balance)
    }
    
    // 상수 정의를 위한 enum 클래스
    private enum class TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER
    }
    
    private enum class TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELED
    }
} 