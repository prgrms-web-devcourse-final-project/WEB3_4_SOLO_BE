package com.pleasybank.authentication.repository

import com.pleasybank.authentication.entity.OAuthProvider
import com.pleasybank.authentication.entity.PasswordReset
import com.pleasybank.authentication.entity.UserAuthentication
import com.pleasybank.authentication.entity.UserOAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface UserAuthenticationRepository : JpaRepository<UserAuthentication, Long> {
    fun findByUserIdAndAuthType(userId: Long, authType: String): Optional<UserAuthentication>
    
    fun existsByUserIdAndAuthType(userId: Long, authType: String): Boolean
    
    fun findByUserIdAndAuthTypeAndIsEnabledTrue(userId: Long, authType: String): Optional<UserAuthentication>
}

interface OAuthProviderRepository : JpaRepository<OAuthProvider, Long> {
    fun findByProviderName(providerName: String): Optional<OAuthProvider>
    
    fun findByIsActiveTrue(): List<OAuthProvider>
}

interface UserOAuthRepository : JpaRepository<UserOAuth, Long> {
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.user.id = :userId AND uo.provider.id = :providerId")
    fun findByUserIdAndProviderId(
        @Param("userId") userId: Long, 
        @Param("providerId") providerId: Long
    ): Optional<UserOAuth>
    
    @Query("SELECT uo FROM UserOAuth uo WHERE uo.provider.id = :providerId AND uo.oauthUserId = :oauthUserId")
    fun findByProviderIdAndOauthUserId(
        @Param("providerId") providerId: Long, 
        @Param("oauthUserId") oauthUserId: String
    ): Optional<UserOAuth>
    
    @Query("SELECT uo FROM UserOAuth uo JOIN FETCH uo.user WHERE uo.provider.id = :providerId AND uo.oauthUserId = :oauthUserId")
    fun findByProviderIdAndOauthUserIdWithUser(
        @Param("providerId") providerId: Long, 
        @Param("oauthUserId") oauthUserId: String
    ): Optional<UserOAuth>
}

interface PasswordResetRepository : JpaRepository<PasswordReset, Long> {
    fun findByToken(token: String): Optional<PasswordReset>
    
    fun findByUserIdAndIsUsedFalseAndExpiresAtAfter(
        userId: Long, 
        currentTime: LocalDateTime
    ): List<PasswordReset>
    
    fun findByIsUsedFalseAndExpiresAtBefore(expirationDate: LocalDateTime): List<PasswordReset>
} 