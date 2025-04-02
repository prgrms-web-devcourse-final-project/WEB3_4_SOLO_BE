package com.pleasybank.integration.openbanking.service

import com.pleasybank.domain.auth.entity.UserOAuth
import com.pleasybank.domain.auth.repository.UserOAuthRepository
import com.pleasybank.integration.openbanking.dto.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

/**
 * 오픈뱅킹 토큰 서비스
 * 오픈뱅킹 토큰 관리 기능을 제공합니다.
 */
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
     * 사용자의 유효한 오픈뱅킹 토큰 조회
     * 토큰이 만료되었거나 없는 경우 null 반환
     */
    @Transactional(readOnly = true)
    fun getValidOpenBankingToken(userId: Long): String? {
        logger.info("유효한 오픈뱅킹 토큰 조회: userId=$userId")
        
        val userOAuthOpt = getUserOAuth(userId)
        if (userOAuthOpt.isEmpty) {
            logger.info("오픈뱅킹 연동 정보 없음: userId=$userId")
            return null
        }
        
        val userOAuth = userOAuthOpt.get()
        
        // 토큰이 없거나 만료된 경우
        if (userOAuth.openBankingAccessToken == null || 
            userOAuth.openBankingTokenExpiresAt == null || 
            userOAuth.openBankingTokenExpiresAt!!.isBefore(LocalDateTime.now())) {
            
            logger.info("오픈뱅킹 토큰 만료 또는 없음: userId=$userId")
            
            // 리프레시 토큰이 있으면 갱신 시도 (실제 구현 필요)
            // TODO: 토큰 갱신 로직 구현
            
            return null
        }
        
        return userOAuth.openBankingAccessToken
    }
    
    /**
     * 사용자의 오픈뱅킹 연동 정보 조회
     */
    @Transactional(readOnly = true)
    fun getUserOAuth(userId: Long): Optional<UserOAuth> {
        logger.info("오픈뱅킹 연동 정보 조회: userId=$userId")
        return userOAuthRepository.findByUserIdWithOpenBankingToken(userId)
    }
    
    /**
     * 오픈뱅킹 토큰 자동 갱신 (매일 새벽 3시 실행)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    fun refreshAllTokens() {
        logger.info("오픈뱅킹 토큰 자동 갱신 시작")
        
        // 구현 예정: 만료 예정인 토큰을 찾아 갱신
        // TODO: 토큰 갱신 구현
        
        logger.info("오픈뱅킹 토큰 자동 갱신 완료")
    }
} 