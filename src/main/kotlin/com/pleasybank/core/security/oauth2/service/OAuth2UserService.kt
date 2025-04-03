package com.pleasybank.core.security.oauth2.service

import com.pleasybank.core.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.*

@Service
class OAuth2UserService : DefaultOAuth2UserService() {
    private val logger = LoggerFactory.getLogger(OAuth2UserService::class.java)
    
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        logger.info("OAuth2 사용자 정보 로딩: provider={}", userRequest.clientRegistration.registrationId)
        
        val oAuth2User = super.loadUser(userRequest)
        
        try {
            return processOAuth2User(userRequest, oAuth2User)
        } catch (ex: Exception) {
            logger.error("OAuth2 인증 처리 중 오류 발생", ex)
            throw OAuth2AuthenticationException("처리 중 오류가 발생했습니다: ${ex.message}")
        }
    }
    
    private fun processOAuth2User(userRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        val attributes = oAuth2User.attributes
        
        logger.debug("OAuth2 사용자 속성: {}", attributes)
        
        // 카카오의 경우 ID는 항상 문자열로 변환
        val userId = when (registrationId) {
            "kakao" -> attributes["id"].toString()
            else -> attributes["id"]?.toString() ?: UUID.randomUUID().toString()
        }
        
        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName
        
        val name = when (registrationId) {
            "kakao" -> {
                val properties = attributes["properties"] as Map<*, *>?
                properties?.get("nickname")?.toString() ?: "이름 없음"
            }
            else -> attributes["name"]?.toString() ?: "이름 없음"
        }
        
        // 프로필 이미지 추출
        val profileImage = when (registrationId) {
            "kakao" -> {
                val properties = attributes["properties"] as Map<*, *>?
                properties?.get("profile_image")?.toString()
            }
            else -> null
        }
        
        // 카카오의 경우 계정 정보에서 이메일 추출
        val email = when (registrationId) {
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as Map<*, *>?
                kakaoAccount?.get("email")?.toString()
            }
            else -> attributes["email"]?.toString()
        }
        
        logger.info("OAuth2 사용자 정보 처리 완료: id={}, name={}, email={}", userId, name, email)
        
        // DefaultOAuth2User 객체 생성
        return DefaultOAuth2User(
            Collections.singleton(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "id" to userId,
                "name" to name,
                "email" to (email ?: ""),
                "profileImage" to (profileImage ?: "")
            ),
            "id"
        )
    }
} 