package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.dto.TokenResponse

/**
 * OAuth2 인증을 위한 서비스 인터페이스
 */
interface OAuth2Service {
    /**
     * 카카오 로그인 처리
     *
     * @param code 카카오 인증 코드
     * @param redirectUri 리다이렉트 URI
     * @return 토큰 응답 객체
     */
    fun processKakaoLogin(code: String, redirectUri: String): TokenResponse
} 