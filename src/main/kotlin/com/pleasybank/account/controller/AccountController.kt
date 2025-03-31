package com.pleasybank.account.controller

import com.pleasybank.account.dto.*
import com.pleasybank.account.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @GetMapping
    fun getAccounts(@AuthenticationPrincipal userId: Long): ResponseEntity<AccountListResponse> {
        // 인증된 사용자의 ID를 가져오는 방식에 따라 다를 수 있음
        // 여기서는 임시로 userId를 직접 받는 형태로 작성
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = accountService.getAccountsList(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{accountId}")
    fun getAccountDetail(
        @AuthenticationPrincipal userId: Long,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountDetailResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = accountService.getAccountDetail(userId, accountId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @AuthenticationPrincipal userId: Long,
        @PathVariable accountId: Long
    ): ResponseEntity<AccountBalanceResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = accountService.getAccountBalance(userId, accountId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{accountId}/transactions")
    fun getAccountTransactions(
        @AuthenticationPrincipal userId: Long,
        @PathVariable accountId: Long,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<AccountTransactionListResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        
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