package com.pleasybank.transaction.controller

import com.pleasybank.transaction.dto.*
import com.pleasybank.transaction.service.ScheduledTransactionService
import com.pleasybank.transaction.service.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val scheduledTransactionService: ScheduledTransactionService
) {

    @PostMapping("/transfer")
    fun transfer(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: TransferRequest
    ): ResponseEntity<TransferResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = transactionService.transfer(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{transactionId}")
    fun getTransactionDetail(
        @AuthenticationPrincipal userId: Long,
        @PathVariable transactionId: Long
    ): ResponseEntity<TransactionDetailResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = transactionService.getTransactionDetail(userId, transactionId)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/{transactionId}/cancel")
    fun cancelTransaction(
        @AuthenticationPrincipal userId: Long,
        @PathVariable transactionId: Long,
        @RequestBody request: TransactionCancelRequest
    ): ResponseEntity<TransactionCancelResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        
        // transactionId가 URL과 요청 본문에 모두 존재하므로 일관성 유지
        if (transactionId != request.transactionId) {
            return ResponseEntity.badRequest().build()
        }
        
        val response = transactionService.cancelTransaction(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/scheduled")
    fun scheduleTransfer(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ScheduledTransferRequest
    ): ResponseEntity<ScheduledTransferResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = scheduledTransactionService.scheduleTransfer(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/scheduled")
    fun getScheduledTransfers(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<ScheduledTransferResponse>> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = scheduledTransactionService.getScheduledTransfers(userId, page, size)
        return ResponseEntity.ok(response)
    }
    
    @DeleteMapping("/scheduled/{scheduledTransactionId}")
    fun cancelScheduledTransfer(
        @AuthenticationPrincipal userId: Long,
        @PathVariable scheduledTransactionId: Long
    ): ResponseEntity<Map<String, Any>> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val result = scheduledTransactionService.cancelScheduledTransfer(userId, scheduledTransactionId)
        return ResponseEntity.ok(mapOf(
            "success" to result,
            "message" to "예약 이체가 성공적으로 취소되었습니다."
        ))
    }
} 