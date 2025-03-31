package com.pleasybank.user.entity

import com.pleasybank.account.entity.Account
import com.pleasybank.authentication.entity.PasswordReset
import com.pleasybank.authentication.entity.UserAuthentication
import com.pleasybank.authentication.entity.UserOAuth
import com.pleasybank.notification.entity.Notification
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Column(nullable = false)
    var name: String,
    
    var phoneNumber: String? = null,
    
    // OAuth2 관련 필드
    var provider: String? = null,
    
    var providerId: String? = null,
    
    var profileImage: String? = null,
    
    // 계정 상태 및 시간 관련 필드
    @Column(nullable = false)
    var status: String, // ACTIVE, INACTIVE, LOCKED
    
    var lastLoginAt: LocalDateTime? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime
) 