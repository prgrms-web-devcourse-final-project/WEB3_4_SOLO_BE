package com.pleasybank.domain.auth.repository

import com.pleasybank.domain.auth.entity.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * OAuth 제공자 레포지토리
 * OAuth 인증 제공자에 대한 데이터베이스 액세스를 제공합니다.
 */
interface OAuthProviderRepository : JpaRepository<OAuthProvider, Long> {
    /**
     * 제공자 이름으로 OAuth 제공자 찾기
     */
    fun findByProviderName(providerName: String): Optional<OAuthProvider>
    
    /**
     * 활성화된 모든 OAuth 제공자 찾기
     */
    fun findByIsActiveTrue(): List<OAuthProvider>
} 