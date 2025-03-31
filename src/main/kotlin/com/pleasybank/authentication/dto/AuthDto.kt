package com.pleasybank.authentication.dto

import java.time.LocalDateTime

// 회원가입 요청/응답
data class SignupRequest(
    val username: String,
    val password: String,
    val confirmPassword: String,
    val email: String,
    val name: String,
    val phone: String
)

data class SignupResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime
)

// 로그인 요청/응답
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
)

// 비밀번호 재설정 요청/응답
data class PasswordResetRequest(
    val email: String
)

data class PasswordResetResponse(
    val message: String
)

// 새 비밀번호 설정 요청/응답
data class NewPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

data class NewPasswordResponse(
    val message: String
)

// PIN 설정 요청/응답
data class PinSetupRequest(
    val pinCode: String
)

data class PinSetupResponse(
    val message: String
)

// PIN 검증 요청/응답
data class PinVerifyRequest(
    val username: String,
    val pinCode: String
)

// 생체인증 설정 요청/응답
data class BiometricSetupRequest(
    val publicKeyCredential: String,
    val biometricType: String
)

data class BiometricSetupResponse(
    val message: String
) 