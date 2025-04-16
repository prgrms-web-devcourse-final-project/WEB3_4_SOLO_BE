package com.pleasybank.domain.transfer.service

import com.pleasybank.domain.transfer.dto.TransactionDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime

interface TransactionService {
    fun transfer(userId: Long, request: TransactionDto.TransferRequest): TransactionDto.Response

    fun deposit(userId: Long, request: TransactionDto.DepositRequest): TransactionDto.Response

    fun withdraw(userId: Long, request: TransactionDto.WithdrawRequest): TransactionDto.Response

    fun getTransactionById(userId: Long, id: Long): TransactionDto.Response

    fun getUserTransactions(userId: Long, pageable: Pageable): Page<TransactionDto.Response>

    fun getAccountTransactions(userId: Long, accountId: Long, pageable: Pageable): Page<TransactionDto.Response>

    fun getTransactionsByDateRange(
        userId: Long,
        accountId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<TransactionDto.Response>

    fun getUserTransactionSummary(
        userId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): TransactionDto.TransactionSummary

    fun getAccountBalance(userId: Long, accountId: Long): BigDecimal
} 