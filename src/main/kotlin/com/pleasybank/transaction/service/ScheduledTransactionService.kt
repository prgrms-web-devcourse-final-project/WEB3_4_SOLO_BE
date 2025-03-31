package com.pleasybank.transaction.service

import com.pleasybank.account.repository.AccountRepository
import com.pleasybank.transaction.dto.ScheduledTransferRequest
import com.pleasybank.transaction.dto.ScheduledTransferResponse
import com.pleasybank.transaction.repository.ScheduledTransactionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ScheduledTransactionService(
    private val scheduledTransactionRepository: ScheduledTransactionRepository,
    private val accountRepository: AccountRepository
) {

    @Transactional
    fun scheduleTransfer(userId: Long, request: ScheduledTransferRequest): ScheduledTransferResponse {
        // 소스 계좌 가져오기
        val sourceAccount = accountRepository.findById(request.sourceAccountId)
            .orElseThrow { IllegalArgumentException("출발 계좌를 찾을 수 없습니다.") }
        
        // 사용자 계좌 소유권 검증
        if (sourceAccount.user.id != userId) {
            throw IllegalAccessException("해당 계좌에 접근 권한이 없습니다.")
        }
        
        // PIN 또는 비밀번호 검증
        val isValidPin = true // 임시로 항상 true 처리
        if (!isValidPin) {
            throw IllegalArgumentException("잘못된 PIN 또는 비밀번호입니다.")
        }
        
        // 목적지 계좌 찾기
        val destinationAccount = accountRepository.findByAccountNumber(request.destinationAccountNumber)
            .orElseThrow { IllegalArgumentException("목적지 계좌를 찾을 수 없습니다.") }
        
        // 예약 시간이 현재 이후인지 확인
        if (request.scheduledDate.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("예약 시간은 현재 시간 이후여야 합니다.")
        }
        
        // 예약 이체 생성
        val scheduledTransaction = com.pleasybank.transaction.entity.ScheduledTransaction(
            sourceAccount = sourceAccount,
            destinationAccount = destinationAccount,
            amount = request.amount,
            description = request.description,
            scheduledDate = request.scheduledDate,
            isRecurring = request.isRecurring,
            recurringPeriod = request.recurringPeriod,
            status = "SCHEDULED",
            createdAt = LocalDateTime.now()
        )
        
        val savedScheduledTransaction = scheduledTransactionRepository.save(scheduledTransaction)
        
        return ScheduledTransferResponse(
            scheduledTransactionId = savedScheduledTransaction.id!!,
            sourceAccountId = sourceAccount.id!!,
            destinationAccountNumber = destinationAccount.accountNumber,
            amount = request.amount,
            scheduledDate = request.scheduledDate,
            isRecurring = request.isRecurring,
            recurringPeriod = request.recurringPeriod,
            status = "SCHEDULED",
            message = "이체가 성공적으로 예약되었습니다."
        )
    }
    
    @Transactional(readOnly = true)
    fun getScheduledTransfers(userId: Long, page: Int, size: Int): List<ScheduledTransferResponse> {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "scheduledDate")
        )
        
        val scheduledTransactions = scheduledTransactionRepository.findByUserIdAndStatusNotIn(
            userId,
            listOf("COMPLETED", "CANCELLED"),
            pageable
        )
        
        return scheduledTransactions.content.map { scheduledTransaction ->
            ScheduledTransferResponse(
                scheduledTransactionId = scheduledTransaction.id!!,
                sourceAccountId = scheduledTransaction.sourceAccount.id!!,
                destinationAccountNumber = scheduledTransaction.destinationAccount.accountNumber,
                amount = scheduledTransaction.amount,
                scheduledDate = scheduledTransaction.scheduledDate,
                isRecurring = scheduledTransaction.isRecurring,
                recurringPeriod = scheduledTransaction.recurringPeriod,
                status = scheduledTransaction.status,
                message = null
            )
        }
    }
    
    @Transactional
    fun cancelScheduledTransfer(userId: Long, scheduledTransactionId: Long): Boolean {
        val scheduledTransaction = scheduledTransactionRepository.findById(scheduledTransactionId)
            .orElseThrow { IllegalArgumentException("예약 이체를 찾을 수 없습니다.") }
        
        // 사용자가 출금 계좌의 소유자인지 확인
        if (scheduledTransaction.sourceAccount.user.id != userId) {
            throw IllegalAccessException("해당 예약 이체를 취소할 권한이 없습니다.")
        }
        
        // 이미 완료되었거나 취소된 경우
        if (scheduledTransaction.status in listOf("COMPLETED", "CANCELLED")) {
            throw IllegalArgumentException("이미 처리되었거나 취소된 예약 이체입니다.")
        }
        
        // 예약 이체 취소
        scheduledTransaction.status = "CANCELLED"
        scheduledTransactionRepository.save(scheduledTransaction)
        
        return true
    }
} 