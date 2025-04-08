package com.pleasybank.integration.kakao.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카카오 토큰 응답 DTO
 * 카카오 인증 서버에서 반환하는 액세스 토큰 정보를 담는 클래스입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    
    @JsonProperty("token_type")
    val tokenType: String,
    
    @JsonProperty("refresh_token")
    val refreshToken: String,
    
    @JsonProperty("expires_in")
    val expiresIn: Int,
    
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int,
    
    @JsonProperty("scope")
    val scope: String? = null
)

/**
 * 카카오 사용자 정보 응답 DTO
 * 카카오 API에서 반환하는 사용자 정보를 담는 클래스입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoUserInfoResponse(
    val id: Long,
    
    @JsonProperty("connected_at")
    val connectedAt: String,
    
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
)

/**
 * 카카오 사용자 프로필 속성 DTO
 * 카카오 사용자의 기본 프로필 정보(닉네임, 프로필 이미지 등)를 담는 클래스입니다.
 */
data class KakaoProperties(
    val nickname: String? = null,
    val profile_image: String? = null,
    val thumbnail_image: String? = null
)

/**
 * 카카오 계정 정보 DTO
 * 카카오 계정의 상세 정보(이메일, 프로필 등)를 담는 클래스입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoAccount(
    @JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean? = null,
    
    @JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean? = null,
    
    val profile: KakaoProfile,
    
    @JsonProperty("has_email")
    val hasEmail: Boolean,
    
    @JsonProperty("email_needs_agreement")
    val emailNeedsAgreement: Boolean? = null,
    
    @JsonProperty("is_email_valid")
    val isEmailValid: Boolean? = null,
    
    @JsonProperty("is_email_verified")
    val isEmailVerified: Boolean? = null,
    
    val email: String? = null,
    
    @JsonProperty("has_age_range")
    val hasAgeRange: Boolean? = null,
    
    @JsonProperty("age_range_needs_agreement")
    val ageRangeNeedsAgreement: Boolean? = null,
    
    @JsonProperty("age_range")
    val ageRange: String? = null,
    
    @JsonProperty("has_gender")
    val hasGender: Boolean? = null,
    
    @JsonProperty("gender_needs_agreement")
    val genderNeedsAgreement: Boolean? = null,
    
    val gender: String? = null
)

/**
 * 카카오 프로필 정보 DTO
 * 카카오 계정의 프로필 세부 정보를 담는 클래스입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoProfile(
    val nickname: String? = null,
    
    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String? = null,
    
    @JsonProperty("profile_image_url")
    val profileImageUrl: String? = null,
    
    @JsonProperty("is_default_image")
    val isDefaultImage: Boolean? = null
) 