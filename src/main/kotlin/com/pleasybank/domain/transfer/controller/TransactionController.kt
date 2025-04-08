package com.pleasybank.domain.transfer.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.transfer.dto.TransactionDto
import com.pleasybank.domain.transfer.service.TransactionService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    fun transferMoney(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: TransactionDto.TransferRequest
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.transfer(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    fun deposit(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: TransactionDto.DepositRequest
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.deposit(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    fun withdraw(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: TransactionDto.WithdrawRequest
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.withdraw(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getTransaction(
        @CurrentUser userId: Long,
        @PathVariable id: Long
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.getTransactionById(userId, id)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getUserTransactions(
        @CurrentUser userId: Long,
        @PageableDefault(size = 20, sort = ["transactionDatetime"]) pageable: Pageable
    ): ResponseEntity<Page<TransactionDto.Response>> {
        val transactions = transactionService.getUserTransactions(userId, pageable)
        return ResponseEntity.ok(transactions)
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("isAuthenticated()")
    fun getAccountTransactions(
        @CurrentUser userId: Long,
        @PathVariable accountId: Long,
        @PageableDefault(size = 20, sort = ["transactionDatetime"]) pageable: Pageable
    ): ResponseEntity<Page<TransactionDto.Response>> {
        val transactions = transactionService.getAccountTransactions(userId, accountId, pageable)
        return ResponseEntity.ok(transactions)
    }
} 