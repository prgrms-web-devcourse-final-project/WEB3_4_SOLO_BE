package com.pleasybank.domain.auth.repository

import com.pleasybank.domain.auth.entity.UserAuthentication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * 사용자 인증 레포지토리
 * 사용자의 인증 정보에 대한 데이터베이스 액세스를 제공합니다.
 */
interface UserAuthenticationRepository : JpaRepository<UserAuthentication, Long> {
    /**
     * 사용자 ID와 인증 유형으로 인증 정보 조회
     */
    @Query("SELECT ua FROM UserAuthentication ua WHERE ua.user.id = :userId AND ua.authType = :type")
    fun findByUserIdAndType(
        @Param("userId") userId: Long, 
        @Param("type") type: String
    ): Optional<UserAuthentication>
    
    /**
     * 사용자 ID와 인증 유형으로 인증 정보가 존재하는지 확인
     */
    @Query("SELECT COUNT(ua) > 0 FROM UserAuthentication ua WHERE ua.user.id = :userId AND ua.authType = :type")
    fun existsByUserIdAndType(
        @Param("userId") userId: Long, 
        @Param("type") type: String
    ): Boolean
    
    /**
     * 사용자 ID와 인증 유형, 활성화 상태로 인증 정보 조회
     */
    fun findByUserIdAndAuthTypeAndIsEnabledTrue(userId: Long, authType: String): Optional<UserAuthentication>
} 