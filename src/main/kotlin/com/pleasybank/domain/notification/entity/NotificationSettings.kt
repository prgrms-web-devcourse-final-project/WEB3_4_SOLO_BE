package com.pleasybank.domain.notification.entity

import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 알림 설정 엔티티
 * 사용자의 알림 수신 설정 정보를 저장합니다.
 */
@Entity
@Table(name = "notification_settings")
data class NotificationSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false, name = "push_enabled")
    var pushEnabled: Boolean = true,
    
    @Column(nullable = false, name = "email_enabled")
    var emailEnabled: Boolean = true,
    
    @Column(nullable = false, name = "sms_enabled")
    var smsEnabled: Boolean = false,
    
    /**
     * 카테고리별 알림 설정을 쉼표로 구분된 문자열로 저장
     * 예: "TRANSACTION:true,SECURITY:true,MARKETING:false,SYSTEM:true"
     */
    @Column(name = "category_settings")
    var categorySettings: String? = null,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 