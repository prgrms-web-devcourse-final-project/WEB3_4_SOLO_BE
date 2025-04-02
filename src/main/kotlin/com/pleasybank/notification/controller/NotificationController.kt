package com.pleasybank.notification.controller

import com.pleasybank.notification.dto.*
import com.pleasybank.notification.service.NotificationService
import com.pleasybank.notification.service.NotificationSettingsService
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
@RequestMapping("/notifications")
@Tag(name = "Notification", description = "알림 관리 API")
class NotificationController(
    private val notificationService: NotificationService,
    private val notificationSettingsService: NotificationSettingsService
) {

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    fun getNotifications(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<NotificationListResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationService.getNotifications(userId, page, size)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{notificationId}")
    @Operation(summary = "알림 상세 조회", description = "특정 알림의 상세 내용을 조회합니다.")
    fun getNotificationDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable notificationId: Long
    ): ResponseEntity<NotificationDetailResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationService.getNotificationDetail(userId, notificationId)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/read")
    @Operation(summary = "알림 일괄 읽음 처리", description = "여러 알림을 한 번에 읽음 상태로 표시합니다.")
    fun markNotificationsAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: MarkNotificationReadRequest
    ): ResponseEntity<MarkNotificationReadResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationService.markNotificationsAsRead(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/settings")
    @Operation(summary = "알림 설정 조회", description = "사용자의 알림 설정을 조회합니다.")
    fun getNotificationSettings(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<NotificationSettingsResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationSettingsService.getNotificationSettings(userId)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/settings")
    @Operation(summary = "알림 설정 변경", description = "사용자의 알림 설정을 변경합니다.")
    fun updateNotificationSettings(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: NotificationSettingsUpdateRequest
    ): ResponseEntity<NotificationSettingsResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationSettingsService.updateNotificationSettings(userId, request)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 표시합니다.")
    fun markAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable notificationId: Long
    ): ResponseEntity<NotificationDetailResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationService.getNotificationDetail(userId, notificationId)
        // 읽음 처리 로직은 서비스에 추가 필요
        return ResponseEntity.ok(response)
    }
}