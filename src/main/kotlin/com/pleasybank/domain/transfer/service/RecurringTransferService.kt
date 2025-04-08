package com.pleasybank.domain.transfer.service

import com.pleasybank.domain.transfer.dto.RecurringTransferDto
import java.time.LocalDate

interface RecurringTransferService {

    fun createRecurringTransfer(
        userId: Long,
        request: RecurringTransferDto.CreateRequest
    ): RecurringTransferDto.Response

    fun getRecurringTransferById(
        userId: Long,
        id: Long
    ): RecurringTransferDto.Response

    fun getUserRecurringTransfers(
        userId: Long
    ): List<RecurringTransferDto.Response>

    fun updateRecurringTransfer(
        userId: Long,
        id: Long,
        request: RecurringTransferDto.UpdateRequest
    ): RecurringTransferDto.Response

    fun cancelRecurringTransfer(
        userId: Long,
        id: Long
    )

    fun executeRecurringTransfers(date: LocalDate = LocalDate.now()): Int
} 