package com.pleasybank.domain.user.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자-역할 매핑 엔티티
 * 사용자와 역할의 다대다 관계를 표현합니다.
 */
@Entity
@Table(name = "user_roles", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "role_id"])])
data class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    val role: Role,
    
    @Column(name = "granted_at", nullable = false)
    val grantedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "granted_by")
    val grantedBy: String? = null
) 