package com.pleasybank.domain.account.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.account.dto.*
import com.pleasybank.domain.account.service.AccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "계좌 API", description = "계좌 관련 API")
class AccountController(
    private val accountService: AccountService
) {

    @Operation(summary = "계좌 생성", description = "사용자 계좌를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createAccount(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: CreateAccountRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.createAccount(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "계좌 상세 조회", description = "지정된 ID의 계좌 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun getAccountById(
        @PathVariable id: Long
    ): ResponseEntity<AccountResponse> {
        val response = accountService.getAccountById(id)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "현재 사용자의 계좌 목록 조회", description = "로그인한 사용자의 계좌 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    fun getMyAccounts(
        @CurrentUser userId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"]) pageable: Pageable
    ): ResponseEntity<Page<AccountResponse>> {
        val response = accountService.getUserAccounts(userId, pageable)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "현재 사용자의 총 잔액 조회", description = "로그인한 사용자의 모든 계좌의 총 잔액을 조회합니다.")
    @GetMapping("/my/total-balance")
    @PreAuthorize("isAuthenticated()")
    fun getMyTotalBalance(
        @CurrentUser userId: Long
    ): ResponseEntity<Map<String, BigDecimal>> {
        val totalBalance = accountService.getUserTotalBalance(userId)
        return ResponseEntity.ok(mapOf("totalBalance" to totalBalance))
    }

    @Operation(summary = "계좌 수정", description = "지정된 ID의 계좌 정보를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    fun updateAccount(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAccountRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.updateAccount(id, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "계좌 해지", description = "지정된 ID의 계좌를 해지합니다.")
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('USER')")
    fun closeAccount(
        @PathVariable id: Long
    ): ResponseEntity<AccountResponse> {
        val response = accountService.closeAccount(id)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "잔액 입금", description = "지정된 ID의 계좌에 금액을 입금합니다.")
    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasRole('USER')")
    fun deposit(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBalanceRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.deposit(id, request.amount, request.description)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "잔액 출금", description = "지정된 ID의 계좌에서 금액을 출금합니다.")
    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('USER')")
    fun withdraw(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBalanceRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.withdraw(id, request.amount, request.description)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "계좌 이체", description = "한 계좌에서 다른 계좌로 금액을 이체합니다.")
    @PostMapping("/{fromId}/transfer/{toId}")
    @PreAuthorize("hasRole('USER')")
    fun transfer(
        @PathVariable fromId: Long,
        @PathVariable toId: Long,
        @Valid @RequestBody request: UpdateBalanceRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.transfer(fromId, toId, request.amount, request.description)
        return ResponseEntity.ok(response)
    }
} 