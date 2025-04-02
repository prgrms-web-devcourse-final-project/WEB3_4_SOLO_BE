package com.pleasybank.domain.user.repository

import com.pleasybank.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 레포지토리
 * 사용자 정보에 대한 데이터베이스 액세스를 제공합니다.
 */
interface UserRepository : JpaRepository<User, Long> {
    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): Optional<User>
    
    /**
     * 이메일로 사용자 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 특정 날짜 이전에 마지막 로그인한 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date")
    fun findByLastLoginBefore(@Param("date") date: LocalDateTime): List<User>
    
    /**
     * 특정 상태의 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    fun findByStatus(@Param("status") status: String): List<User>
} 