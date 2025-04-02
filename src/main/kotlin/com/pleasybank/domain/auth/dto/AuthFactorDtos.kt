package com.pleasybank.domain.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * PIN 설정 요청 DTO
 */
data class PinSetupRequest(
    @field:NotBlank(message = "PIN은 필수 입력값입니다.")
    @field:Size(min = 4, max = 6, message = "PIN은 4-6자리여야 합니다.")
    @field:Pattern(regexp = "^[0-9]+$", message = "PIN은 숫자만 가능합니다.")
    val pin: String
)

/**
 * PIN 설정 응답 DTO
 */
data class PinSetupResponse(
    val message: String
)

/**
 * PIN 확인 요청 DTO
 */
data class PinVerifyRequest(
    val userId: Long,
    
    @field:NotBlank(message = "PIN은 필수 입력값입니다.")
    @field:Size(min = 4, max = 6, message = "PIN은 4-6자리여야 합니다.")
    @field:Pattern(regexp = "^[0-9]+$", message = "PIN은 숫자만 가능합니다.")
    val pin: String
)

/**
 * 생체인증 설정 요청 DTO
 */
data class BiometricSetupRequest(
    @field:NotBlank(message = "공개키는 필수 입력값입니다.")
    val publicKey: String,
    
    val deviceId: String? = null,
    
    val deviceName: String? = null
)

/**
 * 생체인증 설정 응답 DTO
 */
data class BiometricSetupResponse(
    val message: String
) 