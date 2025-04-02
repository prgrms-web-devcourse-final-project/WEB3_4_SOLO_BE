package com.pleasybank.domain.auth.dto

import java.time.LocalDateTime
import com.pleasybank.integration.kakao.dto.KakaoTokenResponse
import com.pleasybank.integration.kakao.dto.KakaoUserInfoResponse

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val user: UserDto?
)

/**
 * 사용자 정보 DTO
 */
data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
    val profileImageUrl: String?
)

/**
 * 회원가입 요청 DTO
 */
data class SignupRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val name: String,
    val phone: String
)

/**
 * 회원가입 응답 DTO
 */
data class SignupResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime
)

/**
 * 비밀번호 재설정 요청 DTO
 */
data class PasswordResetRequest(
    val email: String
)

/**
 * 비밀번호 재설정 응답 DTO
 */
data class PasswordResetResponse(
    val message: String
)

/**
 * 새 비밀번호 설정 요청 DTO
 */
data class NewPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

/**
 * 새 비밀번호 설정 응답 DTO
 */
data class NewPasswordResponse(
    val message: String
)

/**
 * 카카오 인증 요청 DTO
 */
data class KakaoLoginRequest(
    val code: String,
    val redirectUri: String
) 