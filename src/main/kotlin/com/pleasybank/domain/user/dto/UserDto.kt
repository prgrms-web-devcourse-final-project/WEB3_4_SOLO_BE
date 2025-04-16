package com.pleasybank.domain.user.dto

import com.pleasybank.domain.user.entity.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

class UserDto {

    data class CreateRequest(
        @field:NotBlank(message = "이메일은 필수입니다.")
        @field:Email(message = "유효한 이메일 형식이 아닙니다.")
        val email: String,
        
        @field:NotBlank(message = "비밀번호는 필수입니다.")
        @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        val password: String,
        
        @field:NotBlank(message = "이름은 필수입니다.")
        val name: String,
        
        @field:Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다.")
        val phoneNumber: String? = null,
        
        val profileImageUrl: String? = null
    )
    
    data class UpdateRequest(
        val name: String? = null,
        
        @field:Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다.")
        val phoneNumber: String? = null,
        
        val profileImageUrl: String? = null,
        
        val status: String? = null
    )
    
    data class PasswordUpdateRequest(
        @field:NotBlank(message = "현재 비밀번호는 필수입니다.")
        val currentPassword: String,
        
        @field:NotBlank(message = "새 비밀번호는 필수입니다.")
        @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        val newPassword: String,
        
        @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
        val confirmPassword: String
    )
    
    data class Response(
        val id: Long,
        val email: String,
        val name: String,
        val phoneNumber: String?,
        val profileImageUrl: String?,
        val lastLoginAt: LocalDateTime?,
        val status: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val provider: String,
        val providerId: String?
    ) {
        companion object {
            fun fromEntity(user: User): Response {
                return Response(
                    id = user.id ?: 0,
                    email = user.email,
                    name = user.name,
                    phoneNumber = user.phoneNumber,
                    profileImageUrl = user.profileImageUrl,
                    lastLoginAt = user.lastLoginAt,
                    status = user.status,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt,
                    provider = user.provider,
                    providerId = user.providerId
                )
            }
        }
    }
} 