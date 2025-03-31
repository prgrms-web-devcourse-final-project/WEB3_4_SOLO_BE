package com.pleasybank.security.oauth2

abstract class OAuth2UserInfo(
    val attributes: Map<String, Any>
) {
    abstract fun getId(): String
    abstract fun getName(): String?
    abstract fun getEmail(): String?
    abstract fun getImageUrl(): String?
}

class KakaoOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override fun getId(): String {
        return attributes["id"].toString()
    }

    override fun getName(): String? {
        val properties = attributes["properties"] as? Map<String, Any>
        return properties?.get("nickname") as? String
    }

    override fun getEmail(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
        return kakaoAccount?.get("email") as? String
    }

    override fun getImageUrl(): String? {
        val properties = attributes["properties"] as? Map<String, Any>
        return properties?.get("profile_image") as? String
    }
}

object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.toLowerCase()) {
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            // 여기에 다른 OAuth2 제공자(Google, Naver 등)에 대한 구현을 추가할 수 있습니다.
            else -> throw IllegalArgumentException("지원되지 않는 로그인 제공자입니다: $registrationId")
        }
    }
} 