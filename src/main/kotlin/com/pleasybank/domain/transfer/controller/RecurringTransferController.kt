package com.pleasybank.domain.transfer.controller

import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.transfer.dto.RecurringTransferDto
import com.pleasybank.domain.transfer.service.RecurringTransferService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recurring-transfers")
class RecurringTransferController(
    private val recurringTransferService: RecurringTransferService
) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createRecurringTransfer(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: RecurringTransferDto.CreateRequest
    ): ResponseEntity<RecurringTransferDto.Response> {
        val recurringTransfer = recurringTransferService.createRecurringTransfer(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringTransfer)
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getRecurringTransfer(
        @CurrentUser userId: Long,
        @PathVariable id: Long
    ): ResponseEntity<RecurringTransferDto.Response> {
        val recurringTransfer = recurringTransferService.getRecurringTransferById(userId, id)
        return ResponseEntity.ok(recurringTransfer)
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getUserRecurringTransfers(
        @CurrentUser userId: Long
    ): ResponseEntity<List<RecurringTransferDto.Response>> {
        val recurringTransfers = recurringTransferService.getUserRecurringTransfers(userId)
        return ResponseEntity.ok(recurringTransfers)
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun updateRecurringTransfer(
        @CurrentUser userId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: RecurringTransferDto.UpdateRequest
    ): ResponseEntity<RecurringTransferDto.Response> {
        val updatedRecurringTransfer = recurringTransferService.updateRecurringTransfer(userId, id, request)
        return ResponseEntity.ok(updatedRecurringTransfer)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun cancelRecurringTransfer(
        @CurrentUser userId: Long,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        recurringTransferService.cancelRecurringTransfer(userId, id)
        return ResponseEntity.noContent().build()
    }
} 