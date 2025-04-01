package com.pleasybank.notification.entity

import com.pleasybank.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notification_settings")
data class NotificationSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    var pushEnabled: Boolean = true,
    
    var emailEnabled: Boolean = true,
    
    var smsEnabled: Boolean = false,
    
    /**
     * 카테고리별 알림 설정을 쉼표로 구분된 문자열로 저장
     * 예: "TRANSACTION:true,SECURITY:true,MARKETING:false,SYSTEM:true"
     */
    var categorySettings: String? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 