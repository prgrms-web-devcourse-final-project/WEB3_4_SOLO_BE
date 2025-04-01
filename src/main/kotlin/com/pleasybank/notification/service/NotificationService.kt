package com.pleasybank.notification.service

import com.pleasybank.notification.dto.*
import com.pleasybank.notification.entity.Notification
import com.pleasybank.notification.repository.NotificationRepository
import com.pleasybank.notification.repository.NotificationSettingsRepository
import com.pleasybank.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getNotifications(userId: Long, page: Int, size: Int): NotificationListResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )
        
        val notificationsPage = notificationRepository.findByUserId(userId, pageable)
        val unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId)
        
        val notificationSummaries = notificationsPage.content.map { notification ->
            NotificationSummary(
                id = notification.id!!,
                title = notification.title,
                content = notification.content,
                notificationType = notification.notificationType,
                isRead = notification.isRead,
                createdAt = notification.createdAt,
                extraData = notification.extraData?.let { convertStringToMap(it) }
            )
        }
        
        return NotificationListResponse(
            notifications = notificationSummaries,
            unreadCount = unreadCount,
            totalElements = notificationsPage.totalElements,
            totalPages = notificationsPage.totalPages,
            size = notificationsPage.size,
            page = notificationsPage.number
        )
    }
    
    @Transactional(readOnly = true)
    fun getNotificationDetail(userId: Long, notificationId: Long): NotificationDetailResponse {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("알림을 찾을 수 없습니다.") }
        
        // 사용자 권한 확인
        if (notification.user.id != userId) {
            throw IllegalAccessException("해당 알림에 접근 권한이 없습니다.")
        }
        
        return NotificationDetailResponse(
            id = notification.id!!,
            title = notification.title,
            content = notification.content,
            notificationType = notification.notificationType,
            isRead = notification.isRead,
            createdAt = notification.createdAt,
            extraData = notification.extraData?.let { convertStringToMap(it) }
        )
    }
    
    @Transactional
    fun markNotificationsAsRead(userId: Long, request: MarkNotificationReadRequest): MarkNotificationReadResponse {
        // 요청된 알림 ID가 없을 경우
        if (request.notificationIds.isEmpty()) {
            return MarkNotificationReadResponse(
                markedAsRead = 0,
                success = true,
                message = "읽음으로 표시할 알림이 없습니다."
            )
        }
        
        // 사용자의 알림만 읽음 처리하기 위해 사용자 ID로 필터링
        val notifications = notificationRepository.findByUserIdAndIdIn(userId, request.notificationIds)
        
        // 이미 읽은 알림 제외하고 읽음 처리
        val unreadNotifications = notifications.filter { !it.isRead }
        val updatedNotifications = unreadNotifications.map { 
            it.copy(isRead = true) 
        }
        
        if (updatedNotifications.isNotEmpty()) {
            notificationRepository.saveAll(updatedNotifications)
        }
        
        return MarkNotificationReadResponse(
            markedAsRead = updatedNotifications.size,
            success = true,
            message = "${updatedNotifications.size}개의 알림이 읽음으로 표시되었습니다."
        )
    }
    
    @Transactional
    fun createNotification(
        userId: Long,
        title: String,
        content: String,
        notificationType: String,
        extraData: Map<String, Any>? = null
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 사용자의 알림 설정 확인
        val settings = notificationSettingsRepository.findByUserId(userId)
        
        // 해당 유형의 알림이 비활성화되어 있는지 확인
        if (settings != null) {
            val categorySettings = settings.categorySettings?.let { 
                it.split(",").associate { setting ->
                    val parts = setting.split(":")
                    parts[0] to parts[1].toBoolean()
                }
            } ?: emptyMap()
            
            if (categorySettings.containsKey(notificationType) && !categorySettings[notificationType]!!) {
                // 해당 유형의 알림이 비활성화되어 있으면 알림 생성하지 않음
                return
            }
        }
        
        // 알림 데이터를 JSON 문자열로 변환
        val dataString = extraData?.let { convertMapToString(it) }
        
        val notification = Notification(
            user = user,
            title = title,
            content = content,
            notificationType = notificationType,
            isRead = false,
            createdAt = LocalDateTime.now(),
            extraData = dataString
        )
        
        notificationRepository.save(notification)
        
        // 실제 구현에서는 여기서 푸시 알림, 이메일, SMS 등 발송 로직 추가
    }
    
    // Map을 JSON 문자열로 변환하는 유틸리티 함수
    private fun convertMapToString(map: Map<String, Any>): String {
        // 실제 구현에서는 Jackson 같은 라이브러리를 사용하여 JSON 변환
        // 여기서는 간단한 구현을 위해 toString() 사용
        return map.toString()
    }
    
    // JSON 문자열을 Map으로 변환하는 유틸리티 함수
    private fun convertStringToMap(str: String): Map<String, Any> {
        // 실제 구현에서는 Jackson 같은 라이브러리를 사용하여 JSON 파싱
        // 여기서는 간단한 구현을 위해 빈 맵 반환
        return emptyMap()
    }
} 