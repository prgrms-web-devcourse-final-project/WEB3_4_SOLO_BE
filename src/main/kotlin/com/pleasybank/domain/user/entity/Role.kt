package com.pleasybank.domain.user.entity

import jakarta.persistence.*

/**
 * 사용자 역할 엔티티
 * 시스템 내에서 사용자의 권한 역할을 정의합니다.
 */
@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, length = 20)
    val name: String, // ROLE_USER, ROLE_ADMIN 등
    
    @Column(length = 200)
    val description: String? = null
) 