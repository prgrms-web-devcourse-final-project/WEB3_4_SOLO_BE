package com.pleasybank.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(
    @field:NotBlank(message = "인증 코드는 필수 입력값입니다.")
    val code: String,
    
    @field:NotBlank(message = "리다이렉트 URI는 필수 입력값입니다.")
    val redirectUri: String
) 