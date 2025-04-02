package com.pleasybank.domain.auth.repository

import com.pleasybank.domain.auth.entity.UserOAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 OAuth 연결 레포지토리
 * 사용자와 OAuth 제공자 간의 연결 정보에 대한 데이터베이스 액세스를 제공합니다.
 */
interface UserOAuthRepository : JpaRepository<UserOAuth, Long> {
    /**
     * 사용자 ID와 제공자 ID로 연결 정보 찾기
     */
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.user.id = :userId AND uo.provider.id = :providerId")
    fun findByUserIdAndProviderId(
        @Param("userId") userId: Long, 
        @Param("providerId") providerId: Long
    ): Optional<UserOAuth>
    
    /**
     * 제공자 ID와 제공자 사용자 ID로 연결 정보 찾기
     */
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.provider.id = :providerId AND uo.oauthUserId = :oauthUserId")
    fun findByProviderIdAndOauthUserId(
        @Param("providerId") providerId: Long, 
        @Param("oauthUserId") oauthUserId: String
    ): Optional<UserOAuth>
    
    /**
     * 제공자 ID와 제공자 사용자 ID로 연결 정보와 사용자 정보 함께 찾기 (FETCH JOIN)
     */
    @Query("SELECT uo FROM UserOAuth uo JOIN FETCH uo.user WHERE uo.provider.id = :providerId AND uo.oauthUserId = :oauthUserId")
    fun findByProviderIdAndOauthUserIdWithUser(
        @Param("providerId") providerId: Long, 
        @Param("oauthUserId") oauthUserId: String
    ): Optional<UserOAuth>
    
    /**
     * 사용자 ID로 오픈뱅킹 토큰이 있는 연결 정보 찾기
     */
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.user.id = :userId AND uo.openBankingAccessToken IS NOT NULL")
    fun findByUserIdWithOpenBankingToken(@Param("userId") userId: Long): Optional<UserOAuth>
    
    /**
     * 제공자 이름과 제공자 사용자 ID로 연결 정보 찾기
     */
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.provider.providerName = :providerName AND uo.oauthUserId = :oauthUserId")
    fun findByProviderNameAndOauthUserId(
        @Param("providerName") providerName: String,
        @Param("oauthUserId") oauthUserId: String
    ): Optional<UserOAuth>
    
    /**
     * 오픈뱅킹 사용자 일련번호로 연결 정보 찾기
     */
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.openBankingUserSeqNo = :userSeqNo")
    fun findByOpenBankingUserSeqNo(@Param("userSeqNo") userSeqNo: String): Optional<UserOAuth>
    
    /**
     * 만료된 오픈뱅킹 토큰이 있는 모든 연결 정보 찾기
     */
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.openBankingTokenExpiresAt < :currentTime AND uo.openBankingRefreshToken IS NOT NULL")
    fun findAllWithExpiredOpenBankingTokens(@Param("currentTime") currentTime: LocalDateTime): List<UserOAuth>
} 