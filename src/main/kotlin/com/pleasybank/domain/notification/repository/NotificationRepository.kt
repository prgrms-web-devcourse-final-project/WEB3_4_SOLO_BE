package com.pleasybank.domain.notification.repository

import com.pleasybank.domain.notification.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

/**
 * 알림 레포지토리
 * 사용자 알림에 대한 데이터베이스 액세스를 제공합니다.
 */
interface NotificationRepository : JpaRepository<Notification, Long> {
    /**
     * 사용자 ID로 알림 목록 조회
     */
    fun findByUserId(userId: Long, pageable: Pageable): Page<Notification>
    
    /**
     * 사용자 ID와 읽음 상태로 알림 목록 조회
     */
    fun findByUserIdAndIsRead(userId: Long, isRead: Boolean, pageable: Pageable): Page<Notification>
    
    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    
    /**
     * 사용자 ID와 생성일로 알림 목록 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt < :date")
    fun findByUserIdAndCreatedAtBefore(
        @Param("userId") userId: Long,
        @Param("date") date: LocalDateTime,
        pageable: Pageable
    ): Page<Notification>
    
    /**
     * 사용자 ID와 알림 ID 목록으로 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.id IN :notificationIds")
    fun findByUserIdAndIdIn(
        @Param("userId") userId: Long,
        @Param("notificationIds") notificationIds: List<Long>
    ): List<Notification>
} 