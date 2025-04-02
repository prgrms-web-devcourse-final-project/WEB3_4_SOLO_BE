package com.pleasybank.domain.notification.repository

import com.pleasybank.domain.notification.entity.NotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 알림 설정 레포지토리
 * 사용자의 알림 설정에 대한 데이터베이스 액세스를 제공합니다.
 */
@Repository
interface NotificationSettingsRepository : JpaRepository<NotificationSettings, Long> {
    /**
     * 사용자 ID로 알림 설정 조회
     */
    fun findByUserId(userId: Long): NotificationSettings?
} 