package com.pleasybank.domain.auth.entity

import com.pleasybank.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 OAuth 연결 정보 엔티티
 * 사용자와 OAuth 제공자 간의 연결 정보를 저장합니다.
 */
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
    
    @Column(name = "openbanking_access_token")
    var openBankingAccessToken: String? = null,
    
    @Column(name = "openbanking_refresh_token")
    var openBankingRefreshToken: String? = null,
    
    @Column(name = "openbanking_user_seq_no")
    var openBankingUserSeqNo: String? = null,
    
    @Column(name = "openbanking_token_expires_at")
    var openBankingTokenExpiresAt: LocalDateTime? = null,
    
    @Column(name = "openbanking_linked")
    var isOpenBankingLinked: Boolean = false,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 