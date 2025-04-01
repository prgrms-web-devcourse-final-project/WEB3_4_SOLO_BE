package com.pleasybank.notification.service

import com.pleasybank.notification.dto.NotificationSettingsResponse
import com.pleasybank.notification.dto.NotificationSettingsUpdateRequest
import com.pleasybank.notification.entity.NotificationSettings
import com.pleasybank.notification.repository.NotificationSettingsRepository
import com.pleasybank.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationSettingsService(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getNotificationSettings(userId: Long): NotificationSettingsResponse {
        val settings = notificationSettingsRepository.findByUserId(userId)
            ?: return createDefaultSettings(userId)
        
        // 카테고리 설정 문자열을 Map으로 변환
        val categorySettingsMap = settings.categorySettings?.let { 
            it.split(",").associate { setting ->
                val parts = setting.split(":")
                parts[0] to parts[1].toBoolean()
            }
        } ?: emptyMap()
        
        return NotificationSettingsResponse(
            id = settings.id!!,
            userId = settings.user.id!!,
            pushEnabled = settings.pushEnabled,
            emailEnabled = settings.emailEnabled,
            smsEnabled = settings.smsEnabled,
            categorySettings = categorySettingsMap,
            updatedAt = settings.updatedAt
        )
    }
    
    @Transactional
    fun updateNotificationSettings(userId: Long, request: NotificationSettingsUpdateRequest): NotificationSettingsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        val existingSettings = notificationSettingsRepository.findByUserId(userId)
        
        val now = LocalDateTime.now()
        
        // 카테고리 설정을 문자열로 변환
        val categorySettingsString = request.categorySettings.entries.joinToString(",") { 
            "${it.key}:${it.value}" 
        }
        
        val settings = if (existingSettings != null) {
            val updatedSettings = existingSettings.copy(
                pushEnabled = request.pushEnabled,
                emailEnabled = request.emailEnabled,
                smsEnabled = request.smsEnabled,
                categorySettings = categorySettingsString,
                updatedAt = now
            )
            notificationSettingsRepository.save(updatedSettings)
        } else {
            val newSettings = NotificationSettings(
                user = user,
                pushEnabled = request.pushEnabled,
                emailEnabled = request.emailEnabled,
                smsEnabled = request.smsEnabled,
                categorySettings = categorySettingsString,
                createdAt = now,
                updatedAt = now
            )
            notificationSettingsRepository.save(newSettings)
        }
        
        return NotificationSettingsResponse(
            id = settings.id!!,
            userId = settings.user.id!!,
            pushEnabled = settings.pushEnabled,
            emailEnabled = settings.emailEnabled,
            smsEnabled = settings.smsEnabled,
            categorySettings = request.categorySettings,
            updatedAt = settings.updatedAt
        )
    }
    
    // 기본 알림 설정 생성 
    private fun createDefaultSettings(userId: Long): NotificationSettingsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        val now = LocalDateTime.now()
        
        // 기본 카테고리 설정
        val defaultCategorySettings = mapOf(
            "TRANSACTION" to true,
            "SECURITY" to true,
            "MARKETING" to false,
            "SYSTEM" to true
        )
        
        val categorySettingsString = defaultCategorySettings.entries.joinToString(",") { 
            "${it.key}:${it.value}" 
        }
        
        val settings = NotificationSettings(
            user = user,
            pushEnabled = true,
            emailEnabled = true,
            smsEnabled = false,
            categorySettings = categorySettingsString,
            createdAt = now,
            updatedAt = now
        )
        
        val savedSettings = notificationSettingsRepository.save(settings)
        
        return NotificationSettingsResponse(
            id = savedSettings.id!!,
            userId = savedSettings.user.id!!,
            pushEnabled = savedSettings.pushEnabled,
            emailEnabled = savedSettings.emailEnabled,
            smsEnabled = savedSettings.smsEnabled,
            categorySettings = defaultCategorySettings,
            updatedAt = savedSettings.updatedAt
        )
    }
}