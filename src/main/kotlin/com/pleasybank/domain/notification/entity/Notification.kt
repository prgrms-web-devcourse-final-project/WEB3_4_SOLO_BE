package com.pleasybank.domain.notification.entity

import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 알림 엔티티
 * 사용자에게 전송된 알림 정보를 저장합니다.
 */
@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(nullable = false)
    val content: String,
    
    @Column(nullable = false, name = "notification_type")
    val notificationType: String,
    
    @Column(name = "related_id")
    val relatedId: Long? = null,
    
    @Column(nullable = false, name = "is_read")
    val isRead: Boolean = false,
    
    @Column(name = "extra_data", columnDefinition = "TEXT")
    val extraData: String? = null,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) 