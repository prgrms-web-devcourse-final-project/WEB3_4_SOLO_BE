package com.pleasybank.transaction.controller

import com.pleasybank.transaction.dto.*
import com.pleasybank.transaction.service.ScheduledTransactionService
import com.pleasybank.transaction.service.TransactionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction", description = "거래 관리 API")
class TransactionController(
    private val transactionService: TransactionService,
    private val scheduledTransactionService: ScheduledTransactionService
) {

    @PostMapping("/transfer")
    @Operation(summary = "계좌 이체", description = "한 계좌에서 다른 계좌로 자금을 이체합니다.")
    fun transfer(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: TransferRequest
    ): ResponseEntity<TransferResponse> {
        val response = transactionService.transfer(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{transactionId}")
    @Operation(summary = "거래 상세 조회", description = "특정 거래의 상세 정보를 조회합니다.")
    fun getTransactionDetail(
        @AuthenticationPrincipal userId: Long,
        @PathVariable transactionId: Long
    ): ResponseEntity<TransactionDetailResponse> {
        val response = transactionService.getTransactionDetail(userId, transactionId)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/{transactionId}/cancel")
    @Operation(summary = "거래 취소", description = "특정 거래를 취소하고 자금을 원래 계좌로 되돌립니다.")
    fun cancelTransaction(
        @AuthenticationPrincipal userId: Long,
        @PathVariable transactionId: Long,
        @RequestBody request: TransactionCancelRequest
    ): ResponseEntity<TransactionCancelResponse> {
        val updatedRequest = request.copy(transactionId = transactionId)
        val response = transactionService.cancelTransaction(userId, updatedRequest)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/scheduled")
    @Operation(summary = "예약 이체 등록", description = "미래 날짜에 실행될 예약 이체를 등록합니다.")
    fun scheduleTransfer(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ScheduledTransferRequest
    ): ResponseEntity<ScheduledTransferResponse> {
        val response = scheduledTransactionService.scheduleTransfer(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/scheduled")
    @Operation(summary = "예약 이체 목록 조회", description = "사용자의 모든 예약 이체 목록을 조회합니다.")
    fun getScheduledTransfers(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<ScheduledTransferResponse>> {
        val response = scheduledTransactionService.getScheduledTransfers(userId, page, size)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/scheduled/{scheduledTransactionId}")
    @Operation(summary = "예약 이체 취소", description = "예약된 이체를 취소합니다.")
    fun cancelScheduledTransfer(
        @AuthenticationPrincipal userId: Long,
        @PathVariable scheduledTransactionId: Long
    ): ResponseEntity<Void> {
        scheduledTransactionService.cancelScheduledTransfer(userId, scheduledTransactionId)
        return ResponseEntity.noContent().build()
    }
} 