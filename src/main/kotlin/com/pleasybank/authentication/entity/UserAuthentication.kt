package com.pleasybank.authentication.entity

import com.pleasybank.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user_authentications")
data class UserAuthentication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(nullable = false, name = "auth_type")
    val authType: String,
    
    @Column(nullable = false, name = "auth_value")
    val authValue: String,
    
    @Column(nullable = false, name = "is_enabled")
    var isEnabled: Boolean = true,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) 