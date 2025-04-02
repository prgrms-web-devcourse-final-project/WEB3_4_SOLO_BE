package com.pleasybank.domain.auth.repository

import com.pleasybank.domain.auth.entity.PasswordReset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

/**
 * 비밀번호 재설정 레포지토리
 * 사용자의 비밀번호 재설정 요청에 대한 데이터베이스 액세스를 제공합니다.
 */
interface PasswordResetRepository : JpaRepository<PasswordReset, Long> {
    /**
     * 토큰으로 유효한 비밀번호 재설정 요청 조회
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.token = :token AND pr.expiresAt > :now AND pr.isUsed = false")
    fun findByTokenAndNotExpiredAndNotUsed(
        @Param("token") token: String, 
        @Param("now") now: LocalDateTime
    ): Optional<PasswordReset>
    
    /**
     * 토큰으로 비밀번호 재설정 요청 조회
     */
    fun findByToken(token: String): Optional<PasswordReset>
    
    /**
     * 사용자 ID로 사용되지 않고 만료되지 않은 비밀번호 재설정 요청 조회
     */
    fun findByUserIdAndIsUsedFalseAndExpiresAtAfter(
        userId: Long, 
        currentTime: LocalDateTime
    ): List<PasswordReset>
    
    /**
     * 사용되지 않고 만료된 비밀번호 재설정 요청 조회
     */
    fun findByIsUsedFalseAndExpiresAtBefore(expirationDate: LocalDateTime): List<PasswordReset>
} 