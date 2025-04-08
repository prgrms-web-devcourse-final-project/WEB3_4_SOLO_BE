package com.pleasybank.domain.account.service

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.domain.account.dto.*
import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.repository.AccountRepository
import com.pleasybank.domain.transfer.entity.Transaction
import com.pleasybank.domain.transfer.repository.TransactionRepository
import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository
) : AccountService {

    @Transactional
    override fun createAccount(userId: Long, request: CreateAccountRequest): AccountResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: $userId") }
        
        // 기존 계좌번호 중복 체크
        if (accountRepository.findByAccountNumber(request.accountNumber).isPresent) {
            throw IllegalArgumentException("이미 등록된 계좌번호입니다: ${request.accountNumber}")
        }
        
        val account = Account(
            user = user,
            bank = request.bank,
            accountNumber = request.accountNumber,
            accountName = request.accountName,
            accountType = request.accountType,
            balance = request.initialBalance,
            fintechUseNum = request.fintechUseNum
        )
        
        val savedAccount = accountRepository.save(account)
        
        // 초기 잔액이 있는 경우 입금 거래 기록 생성
        if (request.initialBalance > BigDecimal.ZERO) {
            createDepositTransaction(savedAccount, request.initialBalance, "계좌 개설 초기 입금")
        }
        
        return AccountResponse.fromEntity(savedAccount)
    }

    @Transactional(readOnly = true)
    override fun getAccountById(id: Long): AccountResponse {
        val account = findAccountById(id)
        return AccountResponse.fromEntity(account)
    }

    @Transactional(readOnly = true)
    override fun getUserAccounts(userId: Long, pageable: Pageable): Page<AccountResponse> {
        return accountRepository.findByUserId(userId, pageable)
            .map { AccountResponse.fromEntity(it) }
    }

    @Transactional
    override fun updateAccount(id: Long, request: UpdateAccountRequest): AccountResponse {
        val account = findAccountById(id)
        
        // 상태가 CLOSED인 계좌는 수정 불가
        if (account.status == "CLOSED") {
            throw IllegalStateException("이미 해지된 계좌는 수정할 수 없습니다. 계좌 ID: $id")
        }
        
        val updatedAccount = Account(
            id = account.id,
            user = account.user,
            bank = account.bank,
            accountNumber = account.accountNumber,
            accountName = request.accountName ?: account.accountName,
            accountType = account.accountType,
            balance = account.balance,
            fintechUseNum = account.fintechUseNum,
            status = request.status ?: account.status,
            createdAt = account.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(updatedAccount)
        return AccountResponse.fromEntity(savedAccount)
    }

    @Transactional
    override fun closeAccount(id: Long): AccountResponse {
        val account = findAccountById(id)
        
        // 계좌가 이미 해지되었는지 확인
        if (account.status == "CLOSED") {
            throw IllegalStateException("이미 해지된 계좌입니다. 계좌 ID: $id")
        }
        
        // 잔액이 있는 계좌는 해지 불가
        if (account.balance > BigDecimal.ZERO) {
            throw IllegalStateException("잔액이 있는 계좌는 해지할 수 없습니다. 현재 잔액: ${account.balance}")
        }
        
        val closedAccount = Account(
            id = account.id,
            user = account.user,
            bank = account.bank,
            accountNumber = account.accountNumber,
            accountName = account.accountName,
            accountType = account.accountType,
            balance = account.balance,
            fintechUseNum = account.fintechUseNum,
            status = "CLOSED",
            createdAt = account.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(closedAccount)
        return AccountResponse.fromEntity(savedAccount)
    }

    @Transactional
    override fun deposit(id: Long, amount: BigDecimal, description: String?): AccountResponse {
        // 금액이 양수인지 확인
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("입금 금액은 0보다 커야 합니다: $amount")
        }
        
        val account = findAccountById(id)
        
        // 계좌가 활성 상태인지 확인
        validateAccountIsActive(account)
        
        // 잔액 업데이트
        val updatedBalance = account.balance.add(amount)
        
        val updatedAccount = Account(
            id = account.id,
            user = account.user,
            bank = account.bank,
            accountNumber = account.accountNumber,
            accountName = account.accountName,
            accountType = account.accountType,
            balance = updatedBalance,
            fintechUseNum = account.fintechUseNum,
            status = account.status,
            createdAt = account.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(updatedAccount)
        
        // 입금 거래 기록 생성
        createDepositTransaction(savedAccount, amount, description)
        
        return AccountResponse.fromEntity(savedAccount)
    }

    @Transactional
    override fun withdraw(id: Long, amount: BigDecimal, description: String?): AccountResponse {
        // 금액이 양수인지 확인
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("출금 금액은 0보다 커야 합니다: $amount")
        }
        
        val account = findAccountById(id)
        
        // 계좌가 활성 상태인지 확인
        validateAccountIsActive(account)
        
        // 잔액이 충분한지 확인
        if (account.balance < amount) {
            throw IllegalStateException("잔액이 부족합니다. 현재 잔액: ${account.balance}, 출금 요청액: $amount")
        }
        
        // 잔액 업데이트
        val updatedBalance = account.balance.subtract(amount)
        
        val updatedAccount = Account(
            id = account.id,
            user = account.user,
            bank = account.bank,
            accountNumber = account.accountNumber,
            accountName = account.accountName,
            accountType = account.accountType,
            balance = updatedBalance,
            fintechUseNum = account.fintechUseNum,
            status = account.status,
            createdAt = account.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        val savedAccount = accountRepository.save(updatedAccount)
        
        // 출금 거래 기록 생성
        createWithdrawTransaction(savedAccount, amount, description)
        
        return AccountResponse.fromEntity(savedAccount)
    }

    @Transactional
    override fun transfer(fromAccountId: Long, toAccountId: Long, amount: BigDecimal, description: String?): AccountResponse {
        // 자기 자신에게 이체할 수 없음
        if (fromAccountId == toAccountId) {
            throw IllegalArgumentException("같은 계좌로 이체할 수 없습니다")
        }
        
        // 금액이 양수인지 확인
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("이체 금액은 0보다 커야 합니다: $amount")
        }
        
        val fromAccount = findAccountById(fromAccountId)
        val toAccount = findAccountById(toAccountId)
        
        // 두 계좌가 모두 활성 상태인지 확인
        validateAccountIsActive(fromAccount)
        validateAccountIsActive(toAccount)
        
        // 잔액이 충분한지 확인
        if (fromAccount.balance < amount) {
            throw IllegalStateException("잔액이 부족합니다. 현재 잔액: ${fromAccount.balance}, 이체 요청액: $amount")
        }
        
        // 출금 계좌 잔액 업데이트
        val updatedFromBalance = fromAccount.balance.subtract(amount)
        val updatedFromAccount = Account(
            id = fromAccount.id,
            user = fromAccount.user,
            bank = fromAccount.bank,
            accountNumber = fromAccount.accountNumber,
            accountName = fromAccount.accountName,
            accountType = fromAccount.accountType,
            balance = updatedFromBalance,
            fintechUseNum = fromAccount.fintechUseNum,
            status = fromAccount.status,
            createdAt = fromAccount.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        // 입금 계좌 잔액 업데이트
        val updatedToBalance = toAccount.balance.add(amount)
        val updatedToAccount = Account(
            id = toAccount.id,
            user = toAccount.user,
            bank = toAccount.bank,
            accountNumber = toAccount.accountNumber,
            accountName = toAccount.accountName,
            accountType = toAccount.accountType,
            balance = updatedToBalance,
            fintechUseNum = toAccount.fintechUseNum,
            status = toAccount.status,
            createdAt = toAccount.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        accountRepository.save(updatedFromAccount)
        accountRepository.save(updatedToAccount)
        
        // 이체 거래 기록 생성
        createTransferTransaction(updatedFromAccount, updatedToAccount, amount, description)
        
        return AccountResponse.fromEntity(updatedFromAccount)
    }

    @Transactional
    override fun updateBalance(id: Long, request: UpdateBalanceRequest): AccountResponse {
        return when(request.type.uppercase()) {
            "DEPOSIT" -> deposit(id, request.amount, request.description)
            "WITHDRAWAL" -> withdraw(id, request.amount, request.description)
            else -> throw IllegalArgumentException("지원하지 않는, 변경 유형입니다: ${request.type}")
        }
    }

    @Transactional(readOnly = true)
    override fun validateAccount(accountNumber: String, bank: String): Boolean {
        val account = accountRepository.findByAccountNumberAndBank(accountNumber, bank)
        return account.isPresent && account.get().status == "ACTIVE"
    }

    @Transactional(readOnly = true)
    override fun getUserTotalBalance(userId: Long): BigDecimal {
        return accountRepository.sumBalanceByUserId(userId) ?: BigDecimal.ZERO
    }

    @Transactional(readOnly = true)
    override fun getAllAccounts(): List<AccountResponse> {
        return accountRepository.findAll()
            .map { AccountResponse.fromEntity(it) }
    }

    // 헬퍼 메서드
    private fun findAccountById(id: Long): Account {
        return accountRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다. ID: $id") }
    }
    
    private fun validateAccountIsActive(account: Account) {
        if (account.status != "ACTIVE") {
            throw IllegalStateException("계좌가 활성 상태가 아닙니다. 현재 상태: ${account.status}")
        }
    }
    
    private fun createDepositTransaction(account: Account, amount: BigDecimal, description: String?) {
        val transaction = Transaction(
            fromAccount = null,
            toAccount = account,
            amount = amount,
            type = "DEPOSIT",
            description = description ?: "입금",
            transactionDatetime = LocalDateTime.now(),
            status = "COMPLETED"
        )
        transactionRepository.save(transaction)
    }
    
    private fun createWithdrawTransaction(account: Account, amount: BigDecimal, description: String?) {
        val transaction = Transaction(
            fromAccount = account,
            toAccount = null,
            amount = amount,
            type = "WITHDRAWAL",
            description = description ?: "출금",
            transactionDatetime = LocalDateTime.now(),
            status = "COMPLETED"
        )
        transactionRepository.save(transaction)
    }
    
    private fun createTransferTransaction(fromAccount: Account, toAccount: Account, amount: BigDecimal, description: String?) {
        val transaction = Transaction(
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = amount,
            type = "TRANSFER",
            description = description ?: "이체",
            transactionDatetime = LocalDateTime.now(),
            status = "COMPLETED"
        )
        transactionRepository.save(transaction)
    }
} 