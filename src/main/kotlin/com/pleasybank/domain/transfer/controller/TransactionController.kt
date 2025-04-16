package com.pleasybank.domain.transfer.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.auth.model.CustomUserDetails
import com.pleasybank.domain.transfer.dto.TransactionDto
import com.pleasybank.domain.transfer.service.TransactionService
import com.pleasybank.domain.account.repository.AccountRepository
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val accountRepository: AccountRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    fun transferMoney(
        @CurrentUser principal: CustomUserDetails,
        @Valid @RequestBody request: TransactionDto.TransferRequest
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.transfer(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    fun sendMoney(
        @CurrentUser principal: CustomUserDetails,
        @Valid @RequestBody request: TransactionDto.SendRequest
    ): ResponseEntity<TransactionDto.Response> {
        val fromAccountId = request.fromAccountId
        
        // 계좌번호로 계좌 조회
        logger.info("계좌번호 {} 조회 시작", request.toAccountNumber)
        val toAccount = accountRepository.findByAccountNumber(request.toAccountNumber)
            .orElseThrow { IllegalArgumentException("입금 계좌번호를 찾을 수 없습니다: ${request.toAccountNumber}") }
        
        logger.info("계좌번호 {} 조회 성공, 계좌 ID: {}", request.toAccountNumber, toAccount.id)
        
        // 내부 이체 요청 형식으로 변환하여 처리
        val transferRequest = TransactionDto.TransferRequest(
            fromAccountId = fromAccountId,
            toAccountId = toAccount.id!!,
            amount = request.amount,
            description = request.description ?: "계좌이체"
        )
        
        val transaction = transactionService.transfer(principal.id, transferRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    fun deposit(
        @CurrentUser principal: CustomUserDetails,
        @Valid @RequestBody request: TransactionDto.DepositRequest
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.deposit(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    fun withdraw(
        @CurrentUser principal: CustomUserDetails,
        @Valid @RequestBody request: TransactionDto.WithdrawRequest
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.withdraw(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getTransaction(
        @CurrentUser principal: CustomUserDetails,
        @PathVariable id: Long
    ): ResponseEntity<TransactionDto.Response> {
        val transaction = transactionService.getTransactionById(principal.id, id)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getUserTransactions(
        @CurrentUser principal: CustomUserDetails,
        @PageableDefault(size = 20, sort = ["transactionDatetime"]) pageable: Pageable
    ): ResponseEntity<Page<TransactionDto.Response>> {
        val transactions = transactionService.getUserTransactions(principal.id, pageable)
        return ResponseEntity.ok(transactions)
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("isAuthenticated()")
    fun getAccountTransactions(
        @CurrentUser principal: CustomUserDetails,
        @PathVariable accountId: Long,
        @PageableDefault(size = 20, sort = ["transactionDatetime"]) pageable: Pageable
    ): ResponseEntity<Page<TransactionDto.Response>> {
        val transactions = transactionService.getAccountTransactions(principal.id, accountId, pageable)
        return ResponseEntity.ok(transactions)
    }
} 