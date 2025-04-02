package com.pleasybank.domain.transaction.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.core.security.UserPrincipal
import com.pleasybank.domain.transaction.dto.TransactionCreateRequest
import com.pleasybank.domain.transaction.dto.TransactionDetailResponse
import com.pleasybank.domain.transaction.service.OpenBankingTransactionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 거래 컨트롤러
 * 계좌 이체, 일별 거래 내역 요약, 월별 거래 분석 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "거래 API", description = "거래 관련 엔드포인트")
class TransactionController(
    private val openBankingTransactionService: OpenBankingTransactionService
) {
    
    /**
     * 계좌 이체 처리
     */
    @PostMapping("/transfer")
    @Operation(
        summary = "계좌 이체",
        description = "계좌에서 다른 계좌로 송금합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 이체했습니다.",
                content = [Content(schema = Schema(implementation = TransactionDetailResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 또는 오픈뱅킹 연동이 필요합니다."
            )
        ]
    )
    fun transferMoney(
        @CurrentUser userPrincipal: UserPrincipal,
        @RequestBody request: TransactionCreateRequest
    ): ResponseEntity<TransactionDetailResponse> {
        val transaction = openBankingTransactionService.transferMoney(userPrincipal.id, request)
        return ResponseEntity.ok(transaction)
    }
    
    /**
     * 일별 거래 내역 요약
     */
    @GetMapping("/summary/daily")
    @Operation(
        summary = "일별 거래 내역 요약",
        description = "특정 날짜의 거래 내역을 요약하여 제공합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "fintechUseNum", description = "핀테크 이용번호", required = true),
            Parameter(name = "date", description = "조회 날짜(YYYYMMDD)", required = true)
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 요약 정보를 조회했습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 또는 오픈뱅킹 연동이 필요합니다."
            )
        ]
    )
    fun getDailyTransactionSummary(
        @CurrentUser userPrincipal: UserPrincipal,
        @RequestParam fintechUseNum: String,
        @RequestParam date: String
    ): ResponseEntity<Map<String, Any>> {
        val summary = openBankingTransactionService.getDailyTransactionSummary(
            userPrincipal.id, fintechUseNum, date
        )
        return ResponseEntity.ok(summary)
    }
    
    /**
     * 월별 거래 분석
     */
    @GetMapping("/summary/monthly")
    @Operation(
        summary = "월별 거래 분석",
        description = "특정 월의 거래 내역을 분석하여 제공합니다. 오픈뱅킹 연동이 필요합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(name = "fintechUseNum", description = "핀테크 이용번호", required = true),
            Parameter(name = "year", description = "연도", required = true),
            Parameter(name = "month", description = "월(1-12)", required = true)
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공적으로 분석 정보를 조회했습니다."
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 또는 오픈뱅킹 연동이 필요합니다."
            )
        ]
    )
    fun getMonthlyTransactionAnalysis(
        @CurrentUser userPrincipal: UserPrincipal,
        @RequestParam fintechUseNum: String,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<Map<String, Any>> {
        val analysis = openBankingTransactionService.getMonthlyTransactionAnalysis(
            userPrincipal.id, fintechUseNum, year, month
        )
        return ResponseEntity.ok(analysis)
    }
} 