package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.dto.*

/**
 * 인증 요소 서비스 인터페이스
 * PIN 및 생체인증 같은 2차 인증 요소를 관리하는 서비스입니다.
 */
interface AuthFactorService {
    /**
     * PIN 설정
     */
    fun setupPin(userId: Long, request: PinSetupRequest): PinSetupResponse
    
    /**
     * PIN 확인
     */
    fun verifyPin(request: PinVerifyRequest): LoginResponse
    
    /**
     * 생체인증 설정
     */
    fun setupBiometric(userId: Long, request: BiometricSetupRequest): BiometricSetupResponse
} 