package com.pleasybank.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = true)
    val password: String? = null,
    
    @Column(nullable = false)
    val name: String,
    
    val phoneNumber: String? = null,
    
    val profileImageUrl: String? = null,
    
    @Column(nullable = false)
    val status: String = "ACTIVE", // ACTIVE, INACTIVE, BLOCKED, DELETED
    
    @Column(nullable = false)
    val role: String = "ROLE_USER",
    
    @Column(nullable = false)
    val provider: String = "LOCAL", // LOCAL, KAKAO, NAVER, GOOGLE
    
    val providerId: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    val lastLoginAt: LocalDateTime? = null
) 