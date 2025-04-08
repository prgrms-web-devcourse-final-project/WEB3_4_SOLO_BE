package com.pleasybank.domain.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()]).{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    val password: String,
    
    @field:NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
    val confirmPassword: String,
    
    @field:NotBlank(message = "이름은 필수 입력값입니다.")
    val name: String,
    
    @field:Pattern(
        regexp = "^\\d{3}-\\d{3,4}-\\d{4}$",
        message = "유효한 전화번호 형식이 아닙니다. (예: 010-1234-5678)"
    )
    val phoneNumber: String?
)

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
) 