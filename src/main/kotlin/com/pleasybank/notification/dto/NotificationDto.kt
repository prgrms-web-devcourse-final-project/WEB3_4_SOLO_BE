package com.pleasybank.notification.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

// 알림 목록 응답 DTO
data class NotificationListResponse(
    val notifications: List<NotificationSummary>,
    val unreadCount: Int,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int
) {
    data class NotificationSummary(
        val id: Long,
        val title: String,
        val message: String,
        val notificationType: String, // TRANSACTION, SECURITY, MARKETING, SYSTEM 등
        val isRead: Boolean,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        val createdAt: LocalDateTime,
        
        val data: Map<String, Any>? // 추가 데이터 (거래 ID, 계좌 ID 등)
    )
}

// 알림 상세 응답 DTO
data class NotificationDetailResponse(
    val id: Long,
    val title: String,
    val message: String,
    val notificationType: String,
    val isRead: Boolean,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    
    val data: Map<String, Any>?
)

// 알림 읽음 표시 요청 DTO
data class MarkNotificationReadRequest(
    val notificationIds: List<Long>
)

// 알림 읽음 표시 응답 DTO
data class MarkNotificationReadResponse(
    val markedAsRead: Int, // 읽음으로 표시된 알림 개수
    val success: Boolean,
    val message: String?
)

// 알림 설정 응답 DTO
data class NotificationSettingsResponse(
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
    val smsEnabled: Boolean,
    val categories: Map<String, Boolean> // 카테고리별 알림 설정 (예: "TRANSACTION" -> true)
)

// 알림 설정 업데이트 요청 DTO
data class UpdateNotificationSettingsRequest(
    val pushEnabled: Boolean?,
    val emailEnabled: Boolean?,
    val smsEnabled: Boolean?,
    val categories: Map<String, Boolean>?
)

// 알림 설정 업데이트 응답 DTO
data class UpdateNotificationSettingsResponse(
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
    val smsEnabled: Boolean,
    val categories: Map<String, Boolean>,
    val message: String
) 