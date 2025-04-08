package com.pleasybank.domain.transfer.scheduler

import com.pleasybank.domain.account.service.AccountService
import com.pleasybank.domain.transfer.entity.RecurringTransfer
import com.pleasybank.domain.transfer.repository.RecurringTransferRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 자동이체 처리 스케줄러
 * 주기적으로 예정된 자동이체를 처리합니다.
 */
@Component
class RecurringTransferScheduler(
    private val recurringTransferRepository: RecurringTransferRepository,
    private val accountService: AccountService
) {
    private val logger = LoggerFactory.getLogger(RecurringTransferScheduler::class.java)
    
    /**
     * 매시간 자동이체 처리
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 실행
    @Transactional
    fun executeRecurringTransfers() {
        val now = LocalDateTime.now()
        logger.info("자동이체 스케줄러 실행: {}", now)
        
        val dueTransfers = recurringTransferRepository.findDueRecurringTransfers(now.toLocalDate())
        logger.info("실행 예정 자동이체 수: {}", dueTransfers.size)
        
        var successCount = 0
        var failCount = 0
        
        for (recurringTransfer in dueTransfers) {
            try {
                executeTransfer(recurringTransfer)
                successCount++
            } catch (e: Exception) {
                failCount++
                logger.error("자동이체 실행 중 오류 발생. ID: {}, 오류: {}", recurringTransfer.id, e.message)
            }
        }
        
        logger.info("자동이체 실행 완료. 성공: {}, 실패: {}", successCount, failCount)
    }
    
    /**
     * 개별 자동이체 실행
     */
    private fun executeTransfer(recurringTransfer: RecurringTransfer) {
        logger.info("자동이체 실행. ID: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}", 
            recurringTransfer.id, recurringTransfer.fromAccount.id, recurringTransfer.toAccount.id, recurringTransfer.amount)
        
        // 이체 실행
        val fromAccountId = recurringTransfer.fromAccount.id ?: throw IllegalStateException("출금 계좌 ID가 없습니다")
        val toAccountId = recurringTransfer.toAccount.id ?: throw IllegalStateException("입금 계좌 ID가 없습니다")
        
        accountService.transfer(
            fromAccountId,
            toAccountId,
            recurringTransfer.amount,
            recurringTransfer.description.ifEmpty { "자동이체" }
        )
        
        // 다음 실행일 계산
        val nextExecution = calculateNextExecution(recurringTransfer)
        
        // 종료일이 있고 다음 실행일이 종료일 이후라면 자동이체 완료 처리
        val isActive = if (recurringTransfer.endDate != null && nextExecution.toLocalDate().isAfter(recurringTransfer.endDate)) {
            false
        } else {
            recurringTransfer.active
        }
        
        // 자동이체 정보 업데이트
        val updatedTransfer = RecurringTransfer(
            id = recurringTransfer.id,
            user = recurringTransfer.user,
            fromAccount = recurringTransfer.fromAccount,
            toAccount = recurringTransfer.toAccount,
            amount = recurringTransfer.amount,
            frequency = recurringTransfer.frequency,
            description = recurringTransfer.description,
            dayOfWeek = recurringTransfer.dayOfWeek,
            dayOfMonth = recurringTransfer.dayOfMonth,
            startDate = recurringTransfer.startDate,
            endDate = recurringTransfer.endDate,
            nextExecutionDate = nextExecution.toLocalDate(),
            lastExecutionDate = LocalDateTime.now().toLocalDate(),
            active = isActive,
            createdAt = recurringTransfer.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        recurringTransferRepository.save(updatedTransfer)
        logger.info("자동이체 실행 완료. ID: {}, 다음 예정일: {}", recurringTransfer.id, nextExecution)
    }
    
    /**
     * 다음 실행일 계산
     */
    private fun calculateNextExecution(recurringTransfer: RecurringTransfer): LocalDateTime {
        val now = LocalDateTime.now()
        
        return when (recurringTransfer.frequency.uppercase()) {
            "DAILY" -> now.plusDays(1).truncatedTo(ChronoUnit.HOURS)
            "WEEKLY" -> now.plusWeeks(1).truncatedTo(ChronoUnit.HOURS)
            "MONTHLY" -> now.plusMonths(1).truncatedTo(ChronoUnit.HOURS)
            else -> now.plusMonths(1).truncatedTo(ChronoUnit.HOURS) // 기본값 (월간)
        }
    }
} 