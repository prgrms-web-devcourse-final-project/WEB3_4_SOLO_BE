package com.pleasybank.domain.transfer.service

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.domain.account.entity.Account
import com.pleasybank.domain.account.repository.AccountRepository
import com.pleasybank.domain.transfer.dto.RecurringTransferDto
import com.pleasybank.domain.transfer.entity.RecurringTransfer
import com.pleasybank.domain.transfer.repository.RecurringTransferRepository
import com.pleasybank.domain.transfer.repository.TransactionRepository
import com.pleasybank.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class RecurringTransferServiceImpl(
    private val recurringTransferRepository: RecurringTransferRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository
) : RecurringTransferService {

    private val logger = LoggerFactory.getLogger(RecurringTransferServiceImpl::class.java)

    @Transactional
    override fun createRecurringTransfer(
        userId: Long,
        request: RecurringTransferDto.CreateRequest
    ): RecurringTransferDto.Response {
        // 사용자 확인
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다: $userId") }

        // 출금 계좌 확인
        val fromAccount = accountRepository.findById(request.fromAccountId)
            .orElseThrow { ResourceNotFoundException("출금 계좌를 찾을 수 없습니다: ${request.fromAccountId}") }

        // 입금 계좌 확인
        val toAccount = accountRepository.findById(request.toAccountId)
            .orElseThrow { ResourceNotFoundException("입금 계좌를 찾을 수 없습니다: ${request.toAccountId}") }

        // 출금 계좌의 소유자 확인
        if (fromAccount.user.id != userId) {
            throw IllegalArgumentException("출금 계좌의 소유자가 아닙니다")
        }

        // 자동이체 엔티티 생성
        val recurringTransfer = RecurringTransfer(
            user = user,
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = request.amount,
            description = request.description,
            frequency = request.frequency,
            dayOfWeek = request.dayOfWeek,
            dayOfMonth = request.dayOfMonth,
            startDate = request.startDate,
            endDate = request.endDate,
            nextExecutionDate = request.startDate,
            active = request.active,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val savedTransfer = recurringTransferRepository.save(recurringTransfer)
        return RecurringTransferDto.Response.fromEntity(savedTransfer)
    }

    @Transactional(readOnly = true)
    override fun getRecurringTransferById(userId: Long, id: Long): RecurringTransferDto.Response {
        val recurringTransfer = recurringTransferRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("자동이체 설정을 찾을 수 없습니다: $id") }

        // 소유자 확인
        if (recurringTransfer.user.id != userId) {
            throw IllegalArgumentException("해당 자동이체 설정의 소유자가 아닙니다")
        }

        return RecurringTransferDto.Response.fromEntity(recurringTransfer)
    }

    @Transactional(readOnly = true)
    override fun getUserRecurringTransfers(userId: Long): List<RecurringTransferDto.Response> {
        val transfers = recurringTransferRepository.findByFromAccountUserIdAndStatus(userId, "ACTIVE")
        return transfers.map { RecurringTransferDto.Response.fromEntity(it) }
    }

    @Transactional
    override fun updateRecurringTransfer(
        userId: Long,
        id: Long,
        request: RecurringTransferDto.UpdateRequest
    ): RecurringTransferDto.Response {
        val recurringTransfer = recurringTransferRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("자동이체 설정을 찾을 수 없습니다: $id") }

        // 소유자 확인
        if (recurringTransfer.user.id != userId) {
            throw IllegalArgumentException("해당 자동이체 설정의 소유자가 아닙니다")
        }

        // UpdateRequest에는 fromAccountId와 toAccountId가 없으므로 기존 계좌 사용
        val fromAccount = recurringTransfer.fromAccount
        val toAccount = recurringTransfer.toAccount

        // 업데이트된 엔티티 생성
        val updatedTransfer = RecurringTransfer(
            id = recurringTransfer.id,
            user = recurringTransfer.user,
            fromAccount = fromAccount,
            toAccount = toAccount,
            amount = request.amount ?: recurringTransfer.amount,
            description = request.description ?: recurringTransfer.description,
            frequency = request.frequency ?: recurringTransfer.frequency,
            dayOfWeek = request.dayOfWeek ?: recurringTransfer.dayOfWeek,
            dayOfMonth = request.dayOfMonth ?: recurringTransfer.dayOfMonth,
            startDate = request.startDate ?: recurringTransfer.startDate,
            endDate = request.endDate ?: recurringTransfer.endDate,
            nextExecutionDate = recurringTransfer.nextExecutionDate,
            lastExecutionDate = recurringTransfer.lastExecutionDate,
            active = request.active ?: recurringTransfer.active,
            status = recurringTransfer.status,
            createdAt = recurringTransfer.createdAt,
            updatedAt = LocalDateTime.now()
        )

        val savedTransfer = recurringTransferRepository.save(updatedTransfer)
        return RecurringTransferDto.Response.fromEntity(savedTransfer)
    }

    @Transactional
    override fun cancelRecurringTransfer(userId: Long, id: Long) {
        val recurringTransfer = recurringTransferRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("자동이체 설정을 찾을 수 없습니다: $id") }

        // 소유자 확인
        if (recurringTransfer.user.id != userId) {
            throw IllegalArgumentException("해당 자동이체 설정의 소유자가 아닙니다")
        }

        // 비활성화 처리
        val canceledTransfer = RecurringTransfer(
            id = recurringTransfer.id,
            user = recurringTransfer.user,
            fromAccount = recurringTransfer.fromAccount,
            toAccount = recurringTransfer.toAccount,
            amount = recurringTransfer.amount,
            description = recurringTransfer.description,
            frequency = recurringTransfer.frequency,
            dayOfWeek = recurringTransfer.dayOfWeek,
            dayOfMonth = recurringTransfer.dayOfMonth,
            startDate = recurringTransfer.startDate,
            endDate = recurringTransfer.endDate,
            nextExecutionDate = recurringTransfer.nextExecutionDate,
            lastExecutionDate = recurringTransfer.lastExecutionDate,
            active = false,
            status = recurringTransfer.status,
            createdAt = recurringTransfer.createdAt,
            updatedAt = LocalDateTime.now()
        )

        recurringTransferRepository.save(canceledTransfer)
    }

    @Transactional
    override fun executeRecurringTransfers(date: LocalDate): Int {
        logger.info("자동이체 실행 시작: {}", date)
        
        val dueTransfers = recurringTransferRepository.findDueRecurringTransfers(date)
        logger.info("실행 예정 자동이체 수: {}", dueTransfers.size)
        
        var successCount = 0
        
        for (transfer in dueTransfers) {
            try {
                // 자동이체 실행 로직을 여기에 구현
                // transactionService.transfer(...) 등의 로직 구현 필요
                
                // 다음 실행일 계산
                val nextExecution = calculateNextExecution(transfer)
                
                // 업데이트된 자동이체 정보 저장
                val updatedTransfer = RecurringTransfer(
                    id = transfer.id,
                    user = transfer.user,
                    fromAccount = transfer.fromAccount,
                    toAccount = transfer.toAccount,
                    amount = transfer.amount,
                    description = transfer.description,
                    frequency = transfer.frequency,
                    dayOfWeek = transfer.dayOfWeek,
                    dayOfMonth = transfer.dayOfMonth,
                    startDate = transfer.startDate,
                    endDate = transfer.endDate,
                    nextExecutionDate = nextExecution,
                    lastExecutionDate = date,
                    active = transfer.active,
                    status = transfer.status,
                    createdAt = transfer.createdAt,
                    updatedAt = LocalDateTime.now()
                )
                
                recurringTransferRepository.save(updatedTransfer)
                successCount++
                
                logger.info("자동이체 성공: ID={}, 금액={}", transfer.id, transfer.amount)
            } catch (e: Exception) {
                logger.error("자동이체 실행 실패: ID={}, 오류={}", transfer.id, e.message, e)
            }
        }
        
        logger.info("자동이체 실행 완료: 성공={}/{}", successCount, dueTransfers.size)
        return successCount
    }
    
    private fun calculateNextExecution(transfer: RecurringTransfer): LocalDate {
        val today = LocalDate.now()
        
        return when (transfer.frequency.uppercase()) {
            "DAILY" -> today.plusDays(1)
            "WEEKLY" -> today.plusWeeks(1)
            "MONTHLY" -> today.plusMonths(1)
            else -> today.plusMonths(1) // 기본값
        }
    }
} 