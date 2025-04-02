package com.pleasybank.domain.auth.entity

import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 비밀번호 재설정 엔티티
 * 사용자의 비밀번호 재설정 요청 정보를 저장합니다.
 */
@Entity
@Table(name = "password_resets")
data class PasswordReset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false, unique = true)
    val token: String,
    
    @Column(nullable = false, name = "expires_at")
    val expiresAt: LocalDateTime,
    
    @Column(nullable = false, name = "is_used")
    var isUsed: Boolean = false,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) 