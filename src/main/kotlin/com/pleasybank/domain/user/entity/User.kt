package com.pleasybank.domain.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 엔티티
 * 사용자 기본 정보를 저장하는 엔티티입니다.
 */
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    val password: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(name = "phone_number")
    val phoneNumber: String? = null,
    
    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null,
    
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val status: String = "ACTIVE"
) 