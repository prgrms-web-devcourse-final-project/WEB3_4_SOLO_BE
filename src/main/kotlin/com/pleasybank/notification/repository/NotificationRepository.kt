package com.pleasybank.notification.repository

import com.pleasybank.notification.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Notification>
    
    fun findByUserIdAndIsRead(userId: Long, isRead: Boolean, pageable: Pageable): Page<Notification>
    
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt < :date")
    fun findByUserIdAndCreatedAtBefore(
        @Param("userId") userId: Long,
        @Param("date") date: LocalDateTime,
        pageable: Pageable
    ): Page<Notification>
    
    // 사용자별 알림 ID 목록으로 조회
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.id IN :notificationIds")
    fun findByUserIdAndIdIn(
        @Param("userId") userId: Long,
        @Param("notificationIds") notificationIds: List<Long>
    ): List<Notification>
} 