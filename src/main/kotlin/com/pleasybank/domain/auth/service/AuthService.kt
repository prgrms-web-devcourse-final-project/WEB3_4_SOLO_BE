package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.dto.LoginRequest
import com.pleasybank.domain.auth.dto.SignupRequest
import com.pleasybank.domain.auth.dto.TokenResponse

/**
 * 인증 관련 서비스 인터페이스
 */
interface AuthService {

    /**
     * 회원가입 처리
     */
    fun signup(request: SignupRequest): TokenResponse

    /**
     * 로그인 처리
     */
    fun login(request: LoginRequest): TokenResponse

    /**
     * 토큰 갱신
     */
    fun refreshToken(refreshToken: String): TokenResponse

    /**
     * 로그아웃 처리
     */
    fun logout(token: String)

    /**
     * 카카오 로그인 처리
     */
    fun processKakaoLogin(code: String): TokenResponse

    /**
     * 외부 로그인 처리 (OAuth2)
     * @param provider 외부 서비스 제공자 (KAKAO, GOOGLE 등)
     * @param providerId 외부 서비스에서의 사용자 ID
     * @param name 사용자 이름/닉네임
     */
    fun processExternalLogin(provider: String, providerId: String, name: String): TokenResponse
} 