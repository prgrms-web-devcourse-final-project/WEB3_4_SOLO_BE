package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.dto.*

/**
 * 인증 서비스 인터페이스
 * 사용자 가입, 로그인, 비밀번호 재설정, 소셜 로그인 관련 기능을 정의합니다.
 */
interface AuthService {
    /**
     * 회원가입
     */
    fun signup(request: SignupRequest): SignupResponse
    
    /**
     * 로그인
     */
    fun login(request: LoginRequest): LoginResponse
    
    /**
     * 카카오 인증 처리
     */
    fun processKakaoAuth(code: String, redirectUri: String): LoginResponse
    
    /**
     * 비밀번호 재설정 요청
     */
    fun requestPasswordReset(request: PasswordResetRequest): PasswordResetResponse
    
    /**
     * 비밀번호 재설정
     */
    fun resetPassword(request: NewPasswordRequest): NewPasswordResponse
} 