package com.pleasybank.account.controller

import com.pleasybank.account.dto.*
import com.pleasybank.account.service.OpenBankingAccountService
import com.pleasybank.security.CurrentUser
import com.pleasybank.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "계좌 API", description = "계좌 관련 엔드포인트")
class AccountController(
    private val openBankingAccountService: OpenBankingAccountService
) {
    
    /**
     * 사용자의 계좌 목록 조회
     */
    @GetMapping
    @Operation(
        summary = "계좌 목록 조회",
        description = "현재 로그인한 사용자의 계좌 목록을 조회합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 계좌 목록을 조회했습니다.",
                content = [Content(schema = Schema(implementation = AccountListResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "오픈뱅킹 연동이 필요합니다."
            )
        ]
    )
    fun getAccounts(
        @CurrentUser userPrincipal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<AccountListResponse> {
        val accountList = openBankingAccountService.getAccountsByUserId(userPrincipal.id, page, size)
        return ResponseEntity.ok(accountList)
    }
    
    /**
     * 특정 계좌의 상세 정보 조회
     */
    @GetMapping("/{fintechUseNum}")
    @Operation(
        summary = "계좌 상세 정보 조회",
        description = "특정 계좌의 상세 정보를 조회합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "fintechUseNum", description = "핀테크 이용번호", required = true)
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 계좌 정보를 조회했습니다.",
                content = [Content(schema = Schema(implementation = AccountDetailResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "계좌를 찾을 수 없습니다."
            )
        ]
    )
    fun getAccountByFintechNum(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable fintechUseNum: String
    ): ResponseEntity<AccountDetailResponse> {
        val account = openBankingAccountService.getAccountByFintechNum(userPrincipal.id, fintechUseNum)
        return ResponseEntity.ok(account)
    }
    
    /**
     * 계좌 잔액 조회
     */
    @GetMapping("/{fintechUseNum}/balance")
    @Operation(
        summary = "계좌 잔액 조회",
        description = "특정 계좌의 잔액 정보를 조회합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "fintechUseNum", description = "핀테크 이용번호", required = true)
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 잔액 정보를 조회했습니다.",
                content = [Content(schema = Schema(implementation = AccountBalanceResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "계좌를 찾을 수 없습니다."
            )
        ]
    )
    fun getAccountBalance(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable fintechUseNum: String
    ): ResponseEntity<AccountBalanceResponse> {
        val balanceResponse = openBankingAccountService.getAccountBalance(userPrincipal.id, fintechUseNum)
        return ResponseEntity.ok(balanceResponse)
    }
    
    /**
     * 계좌 거래내역 조회
     */
    @GetMapping("/{fintechUseNum}/transactions")
    @Operation(
        summary = "계좌 거래내역 조회",
        description = "특정 계좌의 거래내역을 조회합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "fintechUseNum", description = "핀테크 이용번호", required = true),
            Parameter(name = "fromDate", description = "조회 시작일(YYYYMMDD)", required = true),
            Parameter(name = "toDate", description = "조회 종료일(YYYYMMDD)", required = true),
            Parameter(name = "inquiryType", description = "조회 구분(A: 전체, I: 입금, O: 출금)", required = false)
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 거래내역을 조회했습니다.",
                content = [Content(schema = Schema(implementation = AccountTransactionListResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "계좌를 찾을 수 없습니다."
            )
        ]
    )
    fun getAccountTransactions(
        @CurrentUser userPrincipal: UserPrincipal,
        @PathVariable fintechUseNum: String,
        @RequestParam fromDate: String,
        @RequestParam toDate: String,
        @RequestParam(required = false, defaultValue = "A") inquiryType: String
    ): ResponseEntity<AccountTransactionListResponse> {
        val transactionList = openBankingAccountService.getAccountTransactions(
            userPrincipal.id, fintechUseNum, fromDate, toDate, inquiryType
        )
        return ResponseEntity.ok(transactionList)
    }
} 