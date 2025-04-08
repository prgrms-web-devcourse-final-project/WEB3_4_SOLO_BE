package com.pleasybank.domain.account.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.account.dto.AccountResponse
import com.pleasybank.domain.account.dto.UpdateBalanceRequest
import com.pleasybank.domain.account.service.AccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/accounts")
@Tag(name = "관리자 계좌 API", description = "관리자용 계좌 관리 API")
class AdminAccountController(
    private val accountService: AccountService
) {

    @Operation(summary = "계좌 잔액 추가 (관리자)", description = "관리자가 특정 계좌에 자금을 추가합니다.")
    @PostMapping("/{id}/add-funds")
    @PreAuthorize("hasRole('ADMIN')")
    fun addFundsToAccount(
        @CurrentUser adminId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBalanceRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.deposit(id, request.amount, "관리자에 의한 자금 추가: ${request.description ?: ""}")
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "계좌 목록 조회 (관리자)", description = "관리자가 모든 계좌 목록을 조회합니다.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllAccounts(): ResponseEntity<List<AccountResponse>> {
        val accounts = accountService.getAllAccounts()
        return ResponseEntity.ok(accounts)
    }
} 