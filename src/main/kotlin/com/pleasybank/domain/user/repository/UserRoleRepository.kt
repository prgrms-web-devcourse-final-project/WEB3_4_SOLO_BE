package com.pleasybank.domain.user.repository

import com.pleasybank.domain.user.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRoleRepository : JpaRepository<UserRole, Long> {
    fun findByUserId(userId: Long): List<UserRole>
    
    @Query("SELECT ur FROM UserRole ur JOIN ur.role r WHERE ur.user.id = :userId AND r.name = :roleName")
    fun findByUserIdAndRoleName(@Param("userId") userId: Long, @Param("roleName") roleName: String): List<UserRole>
} 