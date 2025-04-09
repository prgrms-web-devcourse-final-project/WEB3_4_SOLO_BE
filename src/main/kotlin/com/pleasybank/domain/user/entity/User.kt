package com.pleasybank.domain.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 엔티티
 * 시스템의 사용자 정보를 저장하는 엔티티입니다.
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
    var password: String,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(name = "phone_number")
    var phoneNumber: String? = null,
    
    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,
    
    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var status: String = "ACTIVE", // ACTIVE, INACTIVE, BLOCKED
    
    @Column
    var provider: String = "LOCAL", // LOCAL, GOOGLE, KAKAO, etc.
    
    @Column(name = "provider_id")
    var providerId: String? = null,
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userRoles: MutableList<UserRole> = mutableListOf()
) {
    // 사용자 역할 추가 메서드
    fun addRole(role: Role, grantedBy: String? = null): UserRole {
        val userRole = UserRole(
            user = this,
            role = role,
            grantedBy = grantedBy
        )
        userRoles.add(userRole)
        return userRole
    }
    
    // 사용자 역할 제거 메서드
    fun removeRole(role: Role) {
        userRoles.removeIf { it.role.id == role.id }
    }
    
    // 사용자가 특정 역할을 가지고 있는지 확인하는 메서드
    fun hasRole(roleName: String): Boolean {
        return userRoles.any { it.role.name == roleName }
    }
    
    // data 클래스의 copy 대신 동적 필드 업데이트를 위한 메서드
    fun updateOAuth2Info(provider: String, providerId: String): User {
        this.provider = provider
        this.providerId = providerId
        this.lastLoginAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
        return this
    }
} 