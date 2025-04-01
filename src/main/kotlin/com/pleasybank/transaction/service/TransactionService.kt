package com.pleasybank.transaction.service

import com.pleasybank.account.repository.AccountRepository
import com.pleasybank.authentication.service.AuthService
import com.pleasybank.transaction.dto.*
import com.pleasybank.transaction.entity.Transaction
import com.pleasybank.transaction.repository.TransactionRepository
import com.pleasybank.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService
) {

    @Transactional
    fun transfer(userId: Long, request: TransferRequest): TransferResponse {
        // 소스 계좌 가져오기
        val sourceAccount = accountRepository.findById(request.sourceAccountId)
            .orElseThrow { IllegalArgumentException("출발 계좌를 찾을 수 없습니다.") }
        
        // 사용자 계좌 소유권 검증
        if (sourceAccount.user.id != userId) {
            throw IllegalAccessException("해당 계좌에 접근 권한이 없습니다.")
        }
        
        // PIN 또는 비밀번호 검증
        // 실제로는 인증 서비스를 통해 PIN을 검증해야 함
        val isValidPin = true // 임시로 항상 true 처리
        if (!isValidPin) {
            throw IllegalArgumentException("잘못된 PIN 또는 비밀번호입니다.")
        }
        
        // 목적지 계좌 찾기
        val destinationAccount = accountRepository.findByAccountNumber(request.destinationAccountNumber)
            .orElseThrow { IllegalArgumentException("목적지 계좌를 찾을 수 없습니다.") }
        
        // 계좌 잔액 확인
        if (sourceAccount.balance.compareTo(request.amount) < 0) {
            throw IllegalArgumentException("잔액이 부족합니다.")
        }
        
        // 거래 수수료 (실제로는 거래 금액이나 유형에 따라 다를 수 있음)
        val fee = BigDecimal.ZERO
        
        // 계좌 잔액 업데이트 (copy 패턴 사용)
        val updatedSourceAccount = sourceAccount.copy(
            balance = sourceAccount.balance.subtract(request.amount),
            updatedAt = LocalDateTime.now(),
            lastActivityDate = LocalDateTime.now()
        )
        
        val updatedDestinationAccount = destinationAccount.copy(
            balance = destinationAccount.balance.add(request.amount),
            updatedAt = LocalDateTime.now(),
            lastActivityDate = LocalDateTime.now()
        )
        
        accountRepository.save(updatedSourceAccount)
        accountRepository.save(updatedDestinationAccount)
        
        // 거래 정보 저장
        val transactionNumber = generateReferenceNumber()
        val transaction = Transaction(
            transactionNumber = transactionNumber,
            transactionDate = LocalDateTime.now(),
            amount = request.amount,
            totalAmount = request.amount.add(fee),
            fee = fee,
            description = request.description,
            sourceAccount = updatedSourceAccount,
            destinationAccount = updatedDestinationAccount,
            transactionType = "TRANSFER",
            status = "COMPLETED",
            referenceNumber = transactionNumber
        )
        
        val savedTransaction = transactionRepository.save(transaction)
        
        // 응답 생성
        return TransferResponse(
            transactionId = savedTransaction.id!!,
            sourceAccountId = updatedSourceAccount.id!!,
            sourceAccountNumber = updatedSourceAccount.accountNumber,
            destinationAccountNumber = updatedDestinationAccount.accountNumber,
            amount = request.amount,
            fee = fee,
            description = request.description,
            status = "COMPLETED",
            transactionDate = savedTransaction.transactionDate,
            remainingBalance = updatedSourceAccount.balance,
            message = "이체가 성공적으로 완료되었습니다."
        )
    }
    
    @Transactional(readOnly = true)
    fun getTransactionDetail(userId: Long, transactionId: Long): TransactionDetailResponse {
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { IllegalArgumentException("거래 정보를 찾을 수 없습니다.") }
        
        // 사용자가 해당 거래의 소스 계좌나 목적지 계좌의 소유자인지 확인
        val sourceAccount = transaction.sourceAccount
        val destinationAccount = transaction.destinationAccount
        
        // 둘 다 null이면 문제가 있는 거래 데이터
        if (sourceAccount == null && destinationAccount == null) {
            throw IllegalStateException("거래 데이터에 오류가 있습니다.")
        }
        
        // 사용자가 소스 계좌나 목적지 계좌의 소유자인지 확인
        val isSourceAccountOwner = sourceAccount?.user?.id == userId
        val isDestinationAccountOwner = destinationAccount?.user?.id == userId
        
        if (!isSourceAccountOwner && !isDestinationAccountOwner) {
            throw IllegalAccessException("해당 거래 내역에 접근 권한이 없습니다.")
        }
        
        // 응답 생성
        return TransactionDetailResponse(
            id = transaction.id!!,
            transactionNumber = transaction.transactionNumber,
            transactionDate = transaction.transactionDate,
            sourceAccountId = sourceAccount?.id,
            sourceAccountNumber = sourceAccount?.accountNumber,
            sourceAccountName = sourceAccount?.accountName,
            destinationAccountId = destinationAccount?.id,
            destinationAccountNumber = destinationAccount?.accountNumber,
            destinationAccountName = destinationAccount?.accountName,
            amount = transaction.amount,
            fee = transaction.fee,
            totalAmount = transaction.totalAmount,
            description = transaction.description,
            transactionType = transaction.transactionType,
            status = transaction.status,
            referenceNumber = transaction.referenceNumber
        )
    }
    
    @Transactional
    fun cancelTransaction(userId: Long, request: TransactionCancelRequest): TransactionCancelResponse {
        val transaction = transactionRepository.findById(request.transactionId)
            .orElseThrow { IllegalArgumentException("거래 정보를 찾을 수 없습니다.") }
        
        // 거래가 발생한지 24시간 이상 지났는지 확인
        val now = LocalDateTime.now()
        val transactionTime = transaction.transactionDate
        val hoursDifference = java.time.Duration.between(transactionTime, now).toHours()
        
        if (hoursDifference >= 24) {
            throw IllegalArgumentException("거래 취소는 24시간 이내에만 가능합니다.")
        }
        
        // 사용자가 소스 계좌의 소유자인지 확인 (보낸 사람만 취소 가능)
        if (transaction.sourceAccount?.user?.id != userId) {
            throw IllegalAccessException("거래를 취소할 권한이 없습니다.")
        }
        
        // PIN 또는 비밀번호 검증
        val isValidPin = true // 임시로 항상 true 처리
        if (!isValidPin) {
            throw IllegalArgumentException("잘못된 PIN 또는 비밀번호입니다.")
        }
        
        // 이미 취소된 거래인지 확인
        if (transaction.status == "CANCELLED") {
            throw IllegalArgumentException("이미 취소된 거래입니다.")
        }
        
        // 거래 취소 로직 (원래 금액을 다시 이체)
        val sourceAccount = transaction.sourceAccount!!
        val destinationAccount = transaction.destinationAccount!!
        
        // 수취인 계좌에 충분한 잔액이 있는지 확인
        if (destinationAccount.balance.compareTo(transaction.amount) < 0) {
            return TransactionCancelResponse(
                transactionId = 0L,
                originalTransactionId = transaction.id!!,
                status = "FAILED",
                message = "수취인 계좌에 잔액이 부족하여 취소할 수 없습니다."
            )
        }
        
        // 계좌 잔액 업데이트 (copy 패턴 사용)
        val updatedSourceAccount = sourceAccount.copy(
            balance = sourceAccount.balance.add(transaction.amount),
            updatedAt = now,
            lastActivityDate = now
        )
        
        val updatedDestinationAccount = destinationAccount.copy(
            balance = destinationAccount.balance.subtract(transaction.amount),
            updatedAt = now,
            lastActivityDate = now
        )
        
        accountRepository.save(updatedSourceAccount)
        accountRepository.save(updatedDestinationAccount)
        
        // 원래 거래 상태 업데이트 (copy 패턴 사용)
        val updatedTransaction = transaction.copy(status = "CANCELLED")
        transactionRepository.save(updatedTransaction)
        
        // 취소 거래 생성
        val transactionNumber = generateReferenceNumber()
        val cancelTransaction = Transaction(
            transactionNumber = transactionNumber,
            transactionDate = now,
            amount = transaction.amount,
            totalAmount = transaction.amount,
            fee = BigDecimal.ZERO,
            description = "취소: ${transaction.description ?: ""} - 사유: ${request.reason ?: ""}",
            sourceAccount = updatedDestinationAccount,  // 원래 수취인이 출금자가 됨
            destinationAccount = updatedSourceAccount,  // 원래 출금자가 수취인이 됨
            transactionType = "CANCELLATION",
            status = "COMPLETED",
            referenceNumber = transactionNumber,
            relatedTransaction = updatedTransaction
        )
        
        val savedCancelTransaction = transactionRepository.save(cancelTransaction)
        
        return TransactionCancelResponse(
            transactionId = savedCancelTransaction.id!!,
            originalTransactionId = transaction.id!!,
            status = "COMPLETED",
            message = "거래가 성공적으로 취소되었습니다."
        )
    }
    
    // 거래 참조 번호 생성 유틸리티 메서드
    private fun generateReferenceNumber(): String {
        val timestamp = System.currentTimeMillis().toString()
        val randomPart = Random().nextInt(10000).toString().padStart(4, '0')
        return "TRX$timestamp$randomPart"
    }
} 