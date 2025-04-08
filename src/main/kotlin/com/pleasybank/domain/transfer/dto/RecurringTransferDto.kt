package com.pleasybank.domain.transfer.dto

import com.pleasybank.domain.transfer.entity.RecurringTransfer
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class RecurringTransferDto {

    data class CreateRequest(
        @field:NotNull(message = "출금 계좌 ID는 필수입니다.")
        val fromAccountId: Long,
        
        @field:NotNull(message = "입금 계좌 ID는 필수입니다.")
        val toAccountId: Long,
        
        @field:NotNull(message = "금액은 필수입니다.")
        @field:Min(value = 100, message = "최소 이체 금액은 100원입니다.")
        val amount: BigDecimal,
        
        @field:NotBlank(message = "이체 설명은 필수입니다.")
        val description: String,
        
        @field:NotNull(message = "반복 주기는 필수입니다.")
        val frequency: String, // DAILY, WEEKLY, MONTHLY
        
        val dayOfWeek: DayOfWeek? = null, // WEEKLY 주기일 때 사용 (월요일=1, 일요일=7)
        
        val dayOfMonth: Int? = null, // MONTHLY 주기일 때 사용 (1-31)
        
        @field:NotNull(message = "시작일은 필수입니다.")
        val startDate: LocalDate,
        
        val endDate: LocalDate? = null, // null이면 무기한 반복
        
        @field:NotNull(message = "반복 여부는 필수입니다.")
        val active: Boolean = true
    )
    
    data class UpdateRequest(
        val amount: BigDecimal? = null,
        
        val description: String? = null,
        
        val frequency: String? = null, // DAILY, WEEKLY, MONTHLY
        
        val dayOfWeek: DayOfWeek? = null,
        
        val dayOfMonth: Int? = null,
        
        val startDate: LocalDate? = null,
        
        val endDate: LocalDate? = null,
        
        val active: Boolean? = null
    )
    
    data class Response(
        val id: Long,
        val fromAccountId: Long,
        val toAccountId: Long,
        val amount: BigDecimal,
        val description: String,
        val frequency: String,
        val dayOfWeek: DayOfWeek?,
        val dayOfMonth: Int?,
        val startDate: LocalDate,
        val endDate: LocalDate?,
        val nextExecutionDate: LocalDate,
        val lastExecutionDate: LocalDate?,
        val active: Boolean,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun fromEntity(recurringTransfer: RecurringTransfer): Response {
                return Response(
                    id = recurringTransfer.id,
                    fromAccountId = recurringTransfer.fromAccount.id ?: 0L,
                    toAccountId = recurringTransfer.toAccount.id ?: 0L,
                    amount = recurringTransfer.amount,
                    description = recurringTransfer.description,
                    frequency = recurringTransfer.frequency,
                    dayOfWeek = recurringTransfer.dayOfWeek,
                    dayOfMonth = recurringTransfer.dayOfMonth,
                    startDate = recurringTransfer.startDate,
                    endDate = recurringTransfer.endDate,
                    nextExecutionDate = recurringTransfer.nextExecutionDate,
                    lastExecutionDate = recurringTransfer.lastExecutionDate,
                    active = recurringTransfer.active,
                    createdAt = recurringTransfer.createdAt,
                    updatedAt = recurringTransfer.updatedAt
                )
            }
        }
    }
} 