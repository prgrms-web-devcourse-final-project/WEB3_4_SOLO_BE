package com.pleasybank.notification.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

// 알림 목록 응답 DTO
data class NotificationListResponse(
    val notifications: List<NotificationSummary>,
    val unreadCount: Long,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int
)

// 알림 요약 DTO
data class NotificationSummary(
    val id: Long,
    val title: String,
    val content: String,
    val notificationType: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val extraData: Map<String, Any>?
)

// 알림 상세 응답 DTO
data class NotificationDetailResponse(
    val id: Long,
    val title: String,
    val content: String,
    val notificationType: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val extraData: Map<String, Any>?
)

// 알림 읽음 표시 요청 DTO
data class MarkNotificationReadRequest(
    val notificationIds: List<Long>
)

// 알림 읽음 표시 응답 DTO
data class MarkNotificationReadResponse(
    val markedAsRead: Int,
    val success: Boolean,
    val message: String
)

// 알림 설정 응답 DTO
data class NotificationSettingsResponse(
    val id: Long,
    val userId: Long,
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
    val smsEnabled: Boolean,
    val categorySettings: Map<String, Boolean>,
    val updatedAt: LocalDateTime
)

// 알림 설정 업데이트 요청 DTO
data class NotificationSettingsUpdateRequest(
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
    val smsEnabled: Boolean,
    val categorySettings: Map<String, Boolean>
) 