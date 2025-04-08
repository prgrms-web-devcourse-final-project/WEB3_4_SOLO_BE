package com.pleasybank.domain.user.repository

import com.pleasybank.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    
    fun existsByEmail(email: String): Boolean
    
    fun findByProviderAndProviderId(provider: String, providerId: String): User?
} 