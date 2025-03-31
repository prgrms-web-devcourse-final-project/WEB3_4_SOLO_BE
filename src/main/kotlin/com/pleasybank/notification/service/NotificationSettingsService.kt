package com.pleasybank.notification.service

import com.pleasybank.notification.dto.NotificationSettingsResponse
import com.pleasybank.notification.dto.UpdateNotificationSettingsRequest
import com.pleasybank.notification.dto.UpdateNotificationSettingsResponse
import com.pleasybank.notification.repository.NotificationSettingsRepository
import com.pleasybank.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationSettingsService(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getNotificationSettings(userId: Long): NotificationSettingsResponse {
        val settings = notificationSettingsRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("사용자의 알림 설정을 찾을 수 없습니다.")
        
        // 카테고리별 설정 파싱 (예: TRANSACTION:true,SECURITY:true,MARKETING:false)
        val categories = settings.categorySettings?.let { 
            it.split(",").associate { setting ->
                val parts = setting.split(":")
                parts[0] to parts[1].toBoolean()
            }
        } ?: emptyMap()
        
        return NotificationSettingsResponse(
            pushEnabled = settings.pushEnabled,
            emailEnabled = settings.emailEnabled,
            smsEnabled = settings.smsEnabled,
            categories = categories
        )
    }
    
    @Transactional
    fun updateNotificationSettings(
        userId: Long, 
        request: UpdateNotificationSettingsRequest
    ): UpdateNotificationSettingsResponse {
        val settings = notificationSettingsRepository.findByUserId(userId)
            ?: createDefaultNotificationSettings(userId)
        
        // null이 아닌 값만 업데이트
        request.pushEnabled?.let { settings.pushEnabled = it }
        request.emailEnabled?.let { settings.emailEnabled = it }
        request.smsEnabled?.let { settings.smsEnabled = it }
        
        // 카테고리 설정 업데이트
        if (request.categories != null && request.categories.isNotEmpty()) {
            // 기존 카테고리 설정 파싱
            val existingCategories = settings.categorySettings?.let { 
                it.split(",").associate { setting ->
                    val parts = setting.split(":")
                    parts[0] to parts[1].toBoolean()
                }
            } ?: mutableMapOf()
            
            // 새 카테고리 설정 병합
            val updatedCategories = existingCategories.toMutableMap()
            updatedCategories.putAll(request.categories)
            
            // 문자열로 변환하여 저장
            settings.categorySettings = updatedCategories.entries.joinToString(",") { 
                "${it.key}:${it.value}" 
            }
        }
        
        // 업데이트된 설정 저장
        val savedSettings = notificationSettingsRepository.save(settings)
        
        // 카테고리 설정 파싱하여 응답
        val categories = savedSettings.categorySettings?.let { 
            it.split(",").associate { setting ->
                val parts = setting.split(":")
                parts[0] to parts[1].toBoolean()
            }
        } ?: emptyMap()
        
        return UpdateNotificationSettingsResponse(
            pushEnabled = savedSettings.pushEnabled,
            emailEnabled = savedSettings.emailEnabled,
            smsEnabled = savedSettings.smsEnabled,
            categories = categories,
            message = "알림 설정이 성공적으로 업데이트되었습니다."
        )
    }
    
    // 새 사용자를 위한 기본 알림 설정 생성
    private fun createDefaultNotificationSettings(userId: Long): com.pleasybank.notification.entity.NotificationSettings {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        val defaultSettings = com.pleasybank.notification.entity.NotificationSettings(
            user = user,
            pushEnabled = true,
            emailEnabled = true,
            smsEnabled = false,
            categorySettings = "TRANSACTION:true,SECURITY:true,MARKETING:true,SYSTEM:true"
        )
        
        return notificationSettingsRepository.save(defaultSettings)
    }
}