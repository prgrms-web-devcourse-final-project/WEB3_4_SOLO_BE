package com.pleasybank.notification.controller

import com.pleasybank.notification.dto.*
import com.pleasybank.notification.service.NotificationService
import com.pleasybank.notification.service.NotificationSettingsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val notificationSettingsService: NotificationSettingsService
) {

    @GetMapping
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
    fun getNotificationDetail(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable notificationId: Long
    ): ResponseEntity<NotificationDetailResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationService.getNotificationDetail(userId, notificationId)
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/read")
    fun markNotificationsAsRead(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: MarkNotificationReadRequest
    ): ResponseEntity<MarkNotificationReadResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationService.markNotificationsAsRead(userId, request)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/settings")
    fun getNotificationSettings(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<NotificationSettingsResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationSettingsService.getNotificationSettings(userId)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/settings")
    fun updateNotificationSettings(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: NotificationSettingsUpdateRequest
    ): ResponseEntity<NotificationSettingsResponse> {
        val userId = userDetails.username.toLong()
        val response = notificationSettingsService.updateNotificationSettings(userId, request)
        return ResponseEntity.ok(response)
    }
}