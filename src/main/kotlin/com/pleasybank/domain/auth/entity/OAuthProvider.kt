package com.pleasybank.domain.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * OAuth 제공자 엔티티
 * 소셜 로그인 제공자 정보를 저장합니다. (Kakao, Naver, Google 등)
 */
@Entity
@Table(name = "oauth_providers")
data class OAuthProvider(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, name = "provider_name")
    val providerName: String,
    
    @Column(nullable = false, name = "client_id")
    val clientId: String,
    
    @Column(nullable = true, name = "client_secret")
    val clientSecret: String? = null,
    
    @Column(nullable = true, name = "redirect_uri")
    val redirectUri: String? = null,
    
    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "provider", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userConnections: MutableList<UserOAuth> = mutableListOf()
) 