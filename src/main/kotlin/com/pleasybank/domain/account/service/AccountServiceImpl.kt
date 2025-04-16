package com.pleasybank.domain.account.service

import com.pleasybank.core.exception.BadRequestException
import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.core.exception.DuplicateAccountNumberException
import com.pleasybank.domain.account.dto.*
import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.entity.AccountType
import com.pleasybank.domain.account.repository.AccountRepository
import com.pleasybank.domain.product.entity.FinancialProduct
import com.pleasybank.domain.product.repository.FinancialProductRepository
import com.pleasybank.domain.transfer.entity.Transaction
import com.pleasybank.domain.transfer.repository.TransactionRepository
import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val financialProductRepository: FinancialProductRepository
) : AccountService {

    @Transactional
    override fun createAccount(userId: Long, request: CreateAccountRequest): AccountResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: $userId") }
        
        // 기존 계좌번호 중복 체크
        val accountNumber = request.accountNumber
        if (accountRepository.findByAccountNumber(accountNumber).isPresent) {
            throw DuplicateAccountNumberException(accountNumber)
        }
        
        // 계좌 유형 문자열을 AccountType으로 변환
        val accountType = try {
            AccountType.fromString(request.accountType)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("유효하지 않은 계좌 유형입니다: ${request.accountType}")
        }
        
        // 자유입출금(CHECKING) 타입인 경우 최대 2개 제한 체크
        if (accountType == AccountType.CHECKING) {
            val activeCheckingAccounts = accountRepository.findByUserId(userId)
                .filter { it.accountType == AccountType.CHECKING && it.status == "ACTIVE" }
                .count()
                
            if (activeCheckingAccounts >= 2) {
                throw BadRequestException("자유입출금 계좌는 최대 2개까지만 개설할 수 있습니다.")
            }
        }
        
        // 금융상품 연결 (있는 경우)
        val product = request.productId?.let { productId ->
            financialProductRepository.findById(productId)
                .orElse(null)
        }
        
        // 계좌 유형에 따라 금융상품 할당 (지정된 상품이 없는 경우)
        val associatedProduct = if (product == null) {
            when (accountType) {
                AccountType.DEPOSIT -> findDefaultDepositProduct()
                AccountType.SAVINGS -> findDefaultSavingsProduct()
                else -> null
            }
        } else {
            product
        }
        
        // 초기 잔액 검증 - CHECKING과 LOAN 타입이 아닌 경우 0 이상이어야 함
        if (!canCreateWithZeroBalance(accountType) && request.initialBalance <= BigDecimal.ZERO) {
            throw BadRequestException("${accountType} 계좌는 초기 잔액이 0보다 커야 합니다.")
        }
        
        val account = Account(
            user = user,
            bank = request.bank,
            accountNumber = accountNumber,
            accountName = request.accountName,
            accountType = accountType,
            balance = if (accountType == AccountType.LOAN) request.initialBalance.negate() else request.initialBalance,
            fintechUseNum = request.fintechUseNum,
            product = associatedProduct
        )
        
        val savedAccount = accountRepository.save(account)
        
        // 초기 잔액이 있는 경우 입금 거래 기록 생성
        if (request.initialBalance > BigDecimal.ZERO) {
            if (accountType == AccountType.LOAN) {
                // 대출 계좌인 경우 대출 실행으로 표시
                val transaction = Transaction(
                    fromAccount = null,
                    toAccount = savedAccount,
                    amount = request.initialBalance,
                    type = "LOAN_DISBURSEMENT",
                    description = "대출 실행",
                    transactionDatetime = LocalDateTime.now(),
                    status = "COMPLETED"
                )
                transactionRepository.save(transaction)
            } else {
                // 일반 계좌인 경우 입금으로 표시
                createDepositTransaction(savedAccount, request.initialBalance, "계좌 개설 초기 입금")
            }
        }
        
        return AccountResponse.fromEntity(savedAccount)
    }

    // 기본 예금 상품 찾기
    private fun findDefaultDepositProduct(): FinancialProduct? {
        // 예금 상품 중 ID가 9인 상품(이자율 1.8%)을 기본값으로 사용
        return financialProductRepository.findById(9L).orElse(null)
    }
    
    // 기본 적금 상품 찾기
    private fun findDefaultSavingsProduct(): FinancialProduct? {
        // 적금 상품 중 기본 상품 찾기
        return financialProductRepository.findById(1L).orElse(null)
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
        
        // 금융상품 변경 (요청에 있는 경우)
        val product = if (request.productId != null) {
            financialProductRepository.findById(request.productId)
                .orElseThrow { ResourceNotFoundException("금융상품을 찾을 수 없습니다. ID: ${request.productId}") }
        } else {
            account.product
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
            updatedAt = LocalDateTime.now(),
            product = product
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
        
        // 잔액 확인 - 프론트엔드에서 이미 잔액 이체 처리 후 호출하도록 처리됨
        // 백엔드에서는 잔액이 남아 있는지 확인만 하고 해지 진행
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
            updatedAt = LocalDateTime.now(),
            product = account.product
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
        
        // 잔액 업데이트 (대출 계좌인 경우 음수 잔액이 감소)
        val updatedBalance = if (account.accountType == AccountType.LOAN) {
            // 대출 계좌의 경우 잔액을 증가시킴 (대출금 상환, 음수 금액이 줄어듦)
            account.balance.add(amount)
        } else {
            // 일반 계좌의 경우 잔액을 증가시킴
            account.balance.add(amount)
        }
        
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
        
        // 입금 거래 기록 생성 (대출 계좌인 경우 타입을 LOAN_PAYMENT로 설정)
        if (account.accountType == AccountType.LOAN) {
            val transaction = Transaction(
                fromAccount = null,
                toAccount = savedAccount,
                amount = amount,
                type = "LOAN_PAYMENT",
                description = description ?: "대출금 상환",
                transactionDatetime = LocalDateTime.now(),
                status = "COMPLETED"
            )
            transactionRepository.save(transaction)
        } else {
            createDepositTransaction(savedAccount, amount, description)
        }
        
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
        
        // 대출 계좌에서는 출금 불가
        if (account.accountType == AccountType.LOAN) {
            throw IllegalStateException("대출 계좌에서는 출금할 수 없습니다. 계좌 유형: ${account.accountType}")
        }
        
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
        println("===== 이체 시작: ${fromAccountId} → ${toAccountId}, 금액: $amount =====")
        
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
        
        println("출금 계좌 정보: ID=${fromAccount.id}, 유형=${fromAccount.accountType}, 현재 잔액=${fromAccount.balance}")
        println("입금 계좌 정보: ID=${toAccount.id}, 유형=${toAccount.accountType}, 현재 잔액=${toAccount.balance}")
        
        // 두 계좌가 모두 활성 상태인지 확인
        validateAccountIsActive(fromAccount)
        validateAccountIsActive(toAccount)
        
        // 대출 계좌에서는 이체 불가
        if (fromAccount.accountType == AccountType.LOAN) {
            println("대출 계좌에서 이체 시도 - 예외 발생")
            throw IllegalStateException("대출 계좌에서는 이체할 수 없습니다. 계좌 유형: ${fromAccount.accountType}")
        }
        
        // 잔액이 충분한지 확인
        if (fromAccount.balance < amount) {
            println("잔액 부족 - 예외 발생")
            throw IllegalStateException("잔액이 부족합니다. 현재 잔액: ${fromAccount.balance}, 이체 요청액: $amount")
        }
        
        // 출금 계좌 잔액 업데이트
        val updatedFromBalance = fromAccount.balance.subtract(amount)
        println("출금 계좌 업데이트: 기존 잔액=${fromAccount.balance} → 새 잔액=${updatedFromBalance}")
        
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
        
        // 입금 계좌 잔액 업데이트 (대출 계좌인 경우 음수 잔액이 감소)
        val updatedToBalance = if (toAccount.accountType == AccountType.LOAN) {
            // 대출 계좌의 경우 잔액을 증가시킴 (대출금 상환, 음수 금액이 줄어듦)
            val newBalance = toAccount.balance.add(amount)
            println("대출 계좌 상환: 기존 대출 잔액=${toAccount.balance} → 새 대출 잔액=${newBalance}")
            newBalance
        } else {
            // 일반 계좌의 경우 잔액을 증가시킴
            val newBalance = toAccount.balance.add(amount)
            println("일반 계좌 입금: 기존 잔액=${toAccount.balance} → 새 잔액=${newBalance}")
            newBalance
        }
        
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
        
        val savedFromAccount = accountRepository.save(updatedFromAccount)
        val savedToAccount = accountRepository.save(updatedToAccount)
        
        println("저장된 출금 계좌 잔액: ${savedFromAccount.balance}")
        println("저장된 입금 계좌 잔액: ${savedToAccount.balance}")
        
        // 이체 거래 기록 생성 (대출 계좌인 경우 타입을 LOAN_PAYMENT로 설정)
        if (toAccount.accountType == AccountType.LOAN) {
            println("대출 상환 거래 기록 생성: LOAN_PAYMENT")
            val transaction = Transaction(
                fromAccount = fromAccount,
                toAccount = toAccount,
                amount = amount,
                type = "LOAN_PAYMENT",
                description = description ?: "대출금 상환",
                transactionDatetime = LocalDateTime.now(),
                status = "COMPLETED"
            )
            transactionRepository.save(transaction)
        } else {
            println("일반 이체 거래 기록 생성: TRANSFER")
            createTransferTransaction(updatedFromAccount, updatedToAccount, amount, description)
        }
        
        println("===== 이체 완료 =====")
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
            status = "COMPLETED",
            description = description ?: "입금"
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

    // 초기 잔액 없이 생성 가능한 계좌 유형 검증
    override fun canCreateWithZeroBalance(accountType: AccountType): Boolean {
        return accountType == AccountType.CHECKING || accountType == AccountType.LOAN
    }
    
    // 잔액 검증 메서드
    override fun validateSufficientBalance(accountId: Long, amount: BigDecimal): Boolean {
        val account = accountRepository.findById(accountId)
            .orElseThrow { ResourceNotFoundException("계좌를 찾을 수 없습니다. ID: $accountId") }
        
        return account.balance >= amount
    }
    
    // 고유한 계좌번호 생성 헬퍼 메서드
    private fun generateAccountNumber(): String {
        val random = Random()
        val part1 = String.format("%03d", random.nextInt(1000))
        val part2 = String.format("%03d", random.nextInt(1000))
        val part3 = String.format("%06d", random.nextInt(1000000))
        
        return "$part1-$part2-$part3"
    }
} 