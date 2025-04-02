package com.pleasybank.domain.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 사용자 프로필 응답 DTO
 */
data class UserProfileResponse(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val hasPIN: Boolean = false,
    val hasBiometric: Boolean = false,
    val accountCount: Int = 0,
    val totalBalance: BigDecimal = BigDecimal.ZERO,
    val pinAuthEnabled: Boolean = false,
    val bioAuthEnabled: Boolean = false,
    val lastLogin: LocalDateTime? = null,
    val status: String = "ACTIVE"
)

/**
 * 사용자 프로필 수정 요청 DTO
 */
data class UserProfileUpdateRequest(
    @field:Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다.")
    val name: String? = null,
    
    @field:Size(min = 8, max = 15, message = "전화번호는 8~15자 사이여야 합니다.")
    val phoneNumber: String? = null,
    
    val profileImageUrl: String? = null
)

/**
 * 사용자 프로필 수정 응답 DTO
 */
data class UserProfileUpdateResponse(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val updatedAt: LocalDateTime
) 