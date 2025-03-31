package com.pleasybank.notification.controller

import com.pleasybank.notification.dto.*
import com.pleasybank.notification.service.NotificationService
import com.pleasybank.notification.service.NotificationSettingsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val notificationSettingsService: NotificationSettingsService
) {

    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<NotificationListResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = notificationService.getNotifications(userId, page, size)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{notificationId}")
    fun getNotificationDetail(
        @AuthenticationPrincipal userId: Long,
        @PathVariable notificationId: Long
    ): ResponseEntity<NotificationDetailResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = notificationService.getNotificationDetail(userId, notificationId)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/read")
    fun markNotificationsAsRead(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: MarkNotificationReadRequest
    ): ResponseEntity<MarkNotificationReadResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = notificationService.markNotificationsAsRead(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/settings")
    fun getNotificationSettings(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<NotificationSettingsResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = notificationSettingsService.getNotificationSettings(userId)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/settings")
    fun updateNotificationSettings(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: UpdateNotificationSettingsRequest
    ): ResponseEntity<UpdateNotificationSettingsResponse> {
        val userId = 1L // 테스트용 임시 사용자 ID, 실제로는 @AuthenticationPrincipal에서 가져옴
        val response = notificationSettingsService.updateNotificationSettings(userId, request)
        return ResponseEntity.ok(response)
    }
}