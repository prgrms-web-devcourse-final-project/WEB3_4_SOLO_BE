package com.pleasybank.user.dto

import java.math.BigDecimal
import java.time.LocalDateTime

// 로그인 요청 DTO
data class LoginRequest(
    val email: String,
    val password: String
)

// 로그인 응답 DTO
data class LoginResponse(
    val id: Long,
    val email: String,
    val name: String,
    val token: String,
    val refreshToken: String
)

// 회원가입 요청 DTO
data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val phoneNumber: String?
)

// 회원가입 응답 DTO
data class SignupResponse(
    val id: Long,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime
)

// 사용자 정보 업데이트 요청 DTO
data class UserUpdateRequest(
    val name: String?,
    val phoneNumber: String?,
    val currentPassword: String?,
    val newPassword: String?
)

// 사용자 정보 응답 DTO
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String?,
    val createdAt: LocalDateTime
)

// 사용자 프로필 요청 DTO
data class UserProfileRequest(
    val id: Long
)

// 사용자 프로필 응답 DTO
data class UserProfileResponse(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val accountCount: Int,
    val totalBalance: BigDecimal,
    val pinAuthEnabled: Boolean,
    val bioAuthEnabled: Boolean,
    val lastLogin: LocalDateTime?,
    val status: String
)

// 사용자 프로필 업데이트 요청 DTO
data class UserProfileUpdateRequest(
    val name: String,
    val phoneNumber: String?
)

// 사용자 프로필 업데이트 응답 DTO
data class UserProfileUpdateResponse(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val updatedAt: LocalDateTime
)

// 사용자 보안 설정 응답 DTO
data class UserSecuritySettingsResponse(
    val id: Long,
    val email: String,
    val pinAuthEnabled: Boolean,
    val bioAuthEnabled: Boolean,
    val lastPasswordChange: LocalDateTime?
)

// 사용자 비밀번호 변경 요청 DTO
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

// 사용자 비밀번호 변경 응답 DTO
data class ChangePasswordResponse(
    val message: String,
    val updatedAt: LocalDateTime
) 