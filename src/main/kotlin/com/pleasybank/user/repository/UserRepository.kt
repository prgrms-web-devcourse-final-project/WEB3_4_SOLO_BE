package com.pleasybank.user.repository

import com.pleasybank.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    
    fun existsByEmail(email: String): Boolean
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date")
    fun findByLastLoginBefore(@Param("date") date: LocalDateTime): List<User>
    
    @Query("SELECT u FROM User u WHERE u.status = :status")
    fun findByStatus(@Param("status") status: String): List<User>
} 