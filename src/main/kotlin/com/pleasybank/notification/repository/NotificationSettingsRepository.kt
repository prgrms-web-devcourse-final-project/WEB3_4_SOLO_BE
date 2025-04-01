package com.pleasybank.notification.repository

import com.pleasybank.notification.entity.NotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface NotificationSettingsRepository : JpaRepository<NotificationSettings, Long> {
    fun findByUserId(userId: Long): NotificationSettings?
} 