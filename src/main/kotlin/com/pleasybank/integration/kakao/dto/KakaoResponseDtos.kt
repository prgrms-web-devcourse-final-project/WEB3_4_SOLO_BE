package com.pleasybank.integration.kakao.dto

/**
 * 카카오 토큰 응답 DTO
 * 카카오 인증 서버에서 반환하는 액세스 토큰 정보를 담는 클래스입니다.
 */
data class KakaoTokenResponse(
    val access_token: String,
    val token_type: String,
    val refresh_token: String,
    val expires_in: Int,
    val scope: String? = null,
    val refresh_token_expires_in: Int? = null
)

/**
 * 카카오 사용자 정보 응답 DTO
 * 카카오 API에서 반환하는 사용자 정보를 담는 클래스입니다.
 */
data class KakaoUserInfoResponse(
    val id: Long,
    val connected_at: String? = null,
    val properties: KakaoProperties? = null,
    val kakao_account: KakaoAccount? = null
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
data class KakaoAccount(
    val profile_nickname_needs_agreement: Boolean? = null,
    val profile_image_needs_agreement: Boolean? = null,
    val profile: KakaoProfile? = null,
    val has_email: Boolean? = null,
    val email_needs_agreement: Boolean? = null,
    val is_email_valid: Boolean? = null,
    val is_email_verified: Boolean? = null,
    val email: String? = null
)

/**
 * 카카오 프로필 정보 DTO
 * 카카오 계정의 프로필 세부 정보를 담는 클래스입니다.
 */
data class KakaoProfile(
    val nickname: String? = null,
    val thumbnail_image_url: String? = null,
    val profile_image_url: String? = null,
    val is_default_image: Boolean? = null
) 