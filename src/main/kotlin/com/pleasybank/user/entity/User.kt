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
    
    @Column(nullable = false, name = "phone_number")
    val phoneNumber: String,
    
    @Column(name = "birth_date")
    val birthDate: LocalDate? = null,
    
    val address: String? = null,
    
    @Column(nullable = false, name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,
    
    @Column(nullable = false)
    var status: String = "ACTIVE",
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val authentications: MutableList<UserAuthentication> = mutableListOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val oauthConnections: MutableList<UserOAuth> = mutableListOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val passwordResets: MutableList<PasswordReset> = mutableListOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val accounts: MutableList<Account> = mutableListOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val notifications: MutableList<Notification> = mutableListOf()
) 