package com.pleasybank.entity.passwordreset

import com.pleasybank.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

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