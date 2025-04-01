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
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    val password: String,
    
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
    
    val lastLoginAt: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val accounts: MutableList<Account> = mutableListOf()
) 