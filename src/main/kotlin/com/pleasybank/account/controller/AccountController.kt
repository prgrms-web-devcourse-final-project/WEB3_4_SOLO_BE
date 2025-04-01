package com.pleasybank.account.controller

import com.pleasybank.account.dto.*
import com.pleasybank.account.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    fun createAccount(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AccountCreateRequest
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val accountResponse = accountService.createAccount(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse)
    }

    @GetMapping
    fun getAccounts(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<AccountListResponse> {
        val userId = userDetails.username.toLong()
        val accountsResponse = accountService.getAccountsByUserId(userId, page, size)
        return ResponseEntity.ok(accountsResponse)
    }

    @GetMapping("/{accountId}")
    fun getAccountById(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val accountResponse = accountService.getAccountById(accountId, userId)
        return ResponseEntity.ok(accountResponse)
    }

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountBalanceResponse> {
        val userId = userDetails.username.toLong()
        val balanceResponse = accountService.getAccountBalance(accountId, userId)
        return ResponseEntity.ok(balanceResponse)
    }

    @PatchMapping("/{accountId}/status")
    fun updateAccountStatus(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable accountId: Long,
        @RequestParam status: String
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val accountResponse = accountService.updateAccountStatus(accountId, userId, status)
        return ResponseEntity.ok(accountResponse)
    }

    @GetMapping("/{accountId}/transactions")
    fun getAccountTransactions(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable accountId: Long,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<AccountTransactionListResponse> {
        val userId = userDetails.username.toLong()
        val transactionsResponse = accountService.getAccountTransactions(
            userId, 
            accountId, 
            startDate, 
            endDate, 
            page, 
            size
        )
        return ResponseEntity.ok(transactionsResponse)
    }
} 