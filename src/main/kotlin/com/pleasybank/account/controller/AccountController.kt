package com.pleasybank.account.controller

import com.pleasybank.account.dto.*
import com.pleasybank.account.service.AccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account", description = "계좌 관리 API")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    @Operation(summary = "계좌 생성", description = "새로운 계좌를 생성합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "계좌 생성 성공", 
            content = [Content(mediaType = "application/json", 
                schema = Schema(implementation = AccountDetailResponse::class))]),
        ApiResponse(responseCode = "400", description = "잘못된 요청"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    ])
    fun createAccount(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AccountCreateRequest
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val accountResponse = accountService.createAccount(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse)
    }

    @GetMapping
    @Operation(summary = "계좌 목록 조회", description = "사용자의 모든 계좌 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = AccountListResponse::class))]),
        ApiResponse(responseCode = "401", description = "인증 실패")
    ])
    fun getAccounts(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<AccountListResponse> {
        val userId = userDetails.username.toLong()
        val accountsResponse = accountService.getAccountsByUserId(userId, page, size)
        return ResponseEntity.ok(accountsResponse)
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "계좌 상세 조회", description = "특정 계좌의 상세 정보를 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = AccountDetailResponse::class))]),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
    ])
    fun getAccountById(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "계좌 ID") @PathVariable accountId: Long
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val accountResponse = accountService.getAccountById(accountId, userId)
        return ResponseEntity.ok(accountResponse)
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "계좌 잔액 조회", description = "특정 계좌의 현재 잔액을 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = AccountBalanceResponse::class))]),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
    ])
    fun getAccountBalance(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "계좌 ID") @PathVariable accountId: Long
    ): ResponseEntity<AccountBalanceResponse> {
        val userId = userDetails.username.toLong()
        val balanceResponse = accountService.getAccountBalance(accountId, userId)
        return ResponseEntity.ok(balanceResponse)
    }

    @PatchMapping("/{accountId}/status")
    @Operation(summary = "계좌 상태 변경", description = "계좌의 상태를 변경합니다 (ACTIVE, INACTIVE, SUSPENDED, CLOSED).")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "상태 변경 성공",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = AccountDetailResponse::class))]),
        ApiResponse(responseCode = "400", description = "잘못된 상태 값"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
    ])
    fun updateAccountStatus(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "계좌 ID") @PathVariable accountId: Long,
        @Parameter(description = "변경할 상태 (ACTIVE, INACTIVE, SUSPENDED, CLOSED)") @RequestParam status: String
    ): ResponseEntity<AccountDetailResponse> {
        val userId = userDetails.username.toLong()
        val accountResponse = accountService.updateAccountStatus(accountId, userId, status)
        return ResponseEntity.ok(accountResponse)
    }

    @GetMapping("/{accountId}/transactions")
    @Operation(summary = "계좌 거래내역 조회", description = "특정 계좌의 거래내역을 조회합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "조회 성공",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = AccountTransactionListResponse::class))]),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "권한 없음"),
        ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
    ])
    fun getAccountTransactions(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "계좌 ID") @PathVariable accountId: Long,
        @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) startDate: String?,
        @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) endDate: String?,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
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