package com.pleasybank.openbanking.service

import com.pleasybank.authentication.entity.UserOAuth
import com.pleasybank.authentication.repository.UserOAuthRepository
import com.pleasybank.openbanking.dto.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

@Service
class OpenBankingTokenService(
    private val openBankingService: OpenBankingService,
    private val userOAuthRepository: UserOAuthRepository,
    
    @Value("\${openbanking.redirect-uri}")
    private val redirectUri: String
) {
    private val logger = LoggerFactory.getLogger(OpenBankingTokenService::class.java)
    
    /**
     * 사용자의 오픈뱅킹 토큰 정보 저장
     */
    @Transactional
    fun saveOpenBankingToken(userOAuth: UserOAuth, tokenResponse: TokenResponse) {
        logger.info("오픈뱅킹 토큰 정보 저장: userId=${userOAuth.user.id}, userSeqNo=${tokenResponse.user_seq_no}")
        
        // 토큰 만료 시간 계산 (초 단위를 분 단위로 변환하고 안전 마진 5분 적용)
        val expiresInMinutes = tokenResponse.expires_in / 60 - 5
        val expiresAt = LocalDateTime.now().plus(expiresInMinutes.toLong(), ChronoUnit.MINUTES)
        
        userOAuth.openBankingAccessToken = tokenResponse.access_token
        userOAuth.openBankingRefreshToken = tokenResponse.refresh_token
        userOAuth.openBankingUserSeqNo = tokenResponse.user_seq_no
        userOAuth.openBankingTokenExpiresAt = expiresAt
        userOAuth.isOpenBankingLinked = true
        userOAuth.updatedAt = LocalDateTime.now()
        
        userOAuthRepository.save(userOAuth)
        logger.info("오픈뱅킹 토큰 정보 저장 완료: userId=${userOAuth.user.id}")
    }
    
    /**
     * 오픈뱅킹 토큰이 유효한지 확인
     */
    fun isOpenBankingTokenValid(userOAuth: UserOAuth): Boolean {
        if (userOAuth.openBankingAccessToken == null || userOAuth.openBankingTokenExpiresAt == null) {
            return false
        }
        
        return LocalDateTime.now().isBefore(userOAuth.openBankingTokenExpiresAt)
    }
    
    /**
     * 사용자의 오픈뱅킹 토큰 갱신
     */
    @Transactional
    fun refreshOpenBankingToken(userOAuth: UserOAuth): Boolean {
        if (userOAuth.openBankingRefreshToken == null) {
            logger.warn("갱신 토큰이 없어 오픈뱅킹 토큰을 갱신할 수 없습니다: userId=${userOAuth.user.id}")
            return false
        }
        
        try {
            // 토큰 갱신 요청
            val tokenResponse = openBankingService.refreshToken(userOAuth.openBankingRefreshToken!!)
            
            // 새 토큰 정보 저장
            val expiresInMinutes = tokenResponse.expires_in / 60 - 5
            val expiresAt = LocalDateTime.now().plus(expiresInMinutes.toLong(), ChronoUnit.MINUTES)
            
            userOAuth.openBankingAccessToken = tokenResponse.access_token
            userOAuth.openBankingRefreshToken = tokenResponse.refresh_token
            userOAuth.openBankingTokenExpiresAt = expiresAt
            userOAuth.updatedAt = LocalDateTime.now()
            
            userOAuthRepository.save(userOAuth)
            logger.info("오픈뱅킹 토큰 갱신 완료: userId=${userOAuth.user.id}")
            
            return true
        } catch (e: Exception) {
            logger.error("오픈뱅킹 토큰 갱신 실패: userId=${userOAuth.user.id}", e)
            return false
        }
    }
    
    /**
     * 사용자 인증 코드로 토큰 발급 및 저장
     */
    @Transactional
    fun processAuthorizationCode(code: String, userOAuth: UserOAuth): TokenResponse {
        logger.info("오픈뱅킹 인증 코드 처리: userId=${userOAuth.user.id}")
        
        // 코드로 토큰 발급
        val tokenResponse = openBankingService.getToken(code, redirectUri)
        
        // 토큰 정보 저장
        saveOpenBankingToken(userOAuth, tokenResponse)
        
        return tokenResponse
    }
    
    /**
     * 사용자 ID로 유효한 오픈뱅킹 토큰 조회
     * 토큰이 만료됐다면 갱신 시도
     */
    @Transactional
    fun getValidOpenBankingToken(userId: Long): String? {
        val userOAuthOpt = userOAuthRepository.findByUserIdWithOpenBankingToken(userId)
        
        if (userOAuthOpt.isEmpty) {
            logger.info("사용자의 오픈뱅킹 토큰이 없습니다: userId=$userId")
            return null
        }
        
        val userOAuth = userOAuthOpt.get()
        
        // 토큰이 유효한지 확인
        if (isOpenBankingTokenValid(userOAuth)) {
            return userOAuth.openBankingAccessToken
        }
        
        // 토큰이 만료되었다면 갱신 시도
        return if (refreshOpenBankingToken(userOAuth)) {
            userOAuth.openBankingAccessToken
        } else {
            null
        }
    }
    
    /**
     * 사용자 ID로 오픈뱅킹 연동 정보 조회
     */
    fun getUserOAuth(userId: Long): Optional<UserOAuth> {
        return userOAuthRepository.findByUserIdWithOpenBankingToken(userId)
    }
    
    /**
     * 주기적으로 만료 예정인 토큰 갱신 (스케줄링)
     */
    @Scheduled(cron = "0 0 0/1 * * *") // 1시간마다 실행
    @Transactional
    fun refreshExpiredTokens() {
        logger.info("만료된 오픈뱅킹 토큰 갱신 작업 시작")
        
        val currentTime = LocalDateTime.now()
        val expiredTokens = userOAuthRepository.findAllWithExpiredOpenBankingTokens(currentTime)
        
        logger.info("만료된 오픈뱅킹 토큰 수: ${expiredTokens.size}")
        
        var successCount = 0
        for (userOAuth in expiredTokens) {
            if (refreshOpenBankingToken(userOAuth)) {
                successCount++
            }
        }
        
        logger.info("오픈뱅킹 토큰 갱신 완료: 성공=$successCount, 실패=${expiredTokens.size - successCount}")
    }
} 