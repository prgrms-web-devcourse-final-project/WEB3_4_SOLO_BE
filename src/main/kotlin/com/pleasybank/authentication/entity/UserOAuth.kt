package com.pleasybank.authentication.entity

import com.pleasybank.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_oauth")
data class UserOAuth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: OAuthProvider,
    
    @Column(nullable = false, name = "oauth_user_id")
    val oauthUserId: String,
    
    @Column(name = "access_token")
    var accessToken: String? = null,
    
    @Column(name = "refresh_token")
    var refreshToken: String? = null,
    
    @Column(name = "token_expires_at")
    var tokenExpiresAt: LocalDateTime? = null,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 