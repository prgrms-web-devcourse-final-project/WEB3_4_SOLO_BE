package com.pleasybank.domain.auth.entity

import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 인증 엔티티
 * 2차 인증, 생체 인증 등의 인증 정보를 저장합니다.
 */
@Entity
@Table(name = "user_authentications")
data class UserAuthentication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false, name = "auth_type")
    val authType: String,
    
    @Column(nullable = false, name = "auth_value")
    val authValue: String,
    
    @Column(nullable = false, name = "is_enabled")
    var isEnabled: Boolean = true,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 