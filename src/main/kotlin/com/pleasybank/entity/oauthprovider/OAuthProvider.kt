package com.pleasybank.entity.oauthprovider

import com.pleasybank.entity.useroauth.UserOAuth
import jakarta.persistence.*

@Entity
@Table(name = "oauth_providers")
data class OAuthProvider(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, name = "provider_name")
    val providerName: String,
    
    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true,
    
    @OneToMany(mappedBy = "provider", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userConnections: MutableList<UserOAuth> = mutableListOf()
) 