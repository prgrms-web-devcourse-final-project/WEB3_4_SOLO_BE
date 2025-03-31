package com.pleasybank.account.controller

import com.pleasybank.account.dto.*
import com.pleasybank.account.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @GetMapping
    fun getAccounts(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<AccountListResponse> {
        val userId = userDetails.username.toLong()
        val response = accountService.getAccountsList(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{accountId}")
    fun getAccountDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val response = accountService.getAccountDetail(userId, accountId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountBalanceResponse> {
        val userId = userDetails.username.toLong()
        val response = accountService.getAccountBalance(userId, accountId)
        return ResponseEntity.ok(response)
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
        
        val request = AccountTransactionListRequest(
            startDate = startDate,
            endDate = endDate,
            page = page,
            size = size
        )
        
        val response = accountService.getAccountTransactions(userId, accountId, request)
        return ResponseEntity.ok(response)
    }
} 